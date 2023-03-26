package com.hackeruso.automation.selenium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackeruso.automation.conf.EnvConf;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.log4j.Logger;
import org.awaitility.Duration;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DriverFactory implements WebDriver {
    private final WebDriver driver;

    private static final Logger Log = Logger.getLogger(DriverFactory.class.getName());
    private static final Duration WAIT_ELEMENT_TIMEOUT = new Duration(EnvConf.getAsInteger("ui.locator.timeout.sec"), TimeUnit.SECONDS);

    private Actions actions;

    private DriverFactory(WebDriver driver) {
        this.driver = driver;
    }

    public static DriverFactory open(Browser browser, File downloadsFolder) throws IOException, InterruptedException {
        Log.info(String.format("Starting new %s browser driver", browser));
        switch (browser) {
            case FIREFOX:
                return createFireFoxInst();
            case CHROME:
                return createChromeInst(downloadsFolder);
            default:
                throw new IllegalArgumentException("'" + browser + "'no such browser type");
        }
    }

    private static DriverFactory createFireFoxInst() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        options.setAcceptInsecureCerts(true);
        options.setHeadless((EnvConf.getAsBoolean("selenium.headless")));
        FirefoxDriver driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        return new DriverFactory(driver);
    }

    public File getScreenshotAsFile() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    }

    public byte[] getScreenshotAsByte() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
    }

    private static DriverFactory createChromeInst(File downloadsFolder) {
        System.setProperty("webdriver.chrome.whitelistedIps", "");
        Map<String, Object> pref = new Hashtable<>();
        pref.put("download.default_directory", EnvConf.getProperty("workspace.tests.downloads"));

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(EnvConf.getAsBoolean("selenium.headless"));
        options.setExperimentalOption("prefs", pref);
        options.setAcceptInsecureCerts(true);
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-popup-blocking");
        //options.addExtensions(new File(FileUtil.getFile(EnvConf.getProperty("automation.recaptcha.crx"))));
        //options.addExtensions(new File(FileUtil.getFile(EnvConf.getProperty("automation.chropath.crx"))));
//        options.setExperimentalOption("excludeSwitches", Arrays.asList("disable-popup-blocking"));
        options.addArguments("--lang=" + EnvConf.getProperty("selenium.locale"));
        options.addArguments("--window-size=" + EnvConf.getProperty("selenium.window_size"));

        DesiredCapabilities dc = new DesiredCapabilities();
        dc.setCapability(ChromeOptions.CAPABILITY, options);
        options.merge(dc);
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.SEVERE);
        options.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

        if (!EnvConf.getAsBoolean("selenium.headless")) {
            options.addArguments("--disable-gpu");
        }

        ChromeDriverService service = ChromeDriverService.createDefaultService();
        ChromeDriver driver = new ChromeDriver(service, options);

        if (!EnvConf.getAsBoolean("selenium.headless")) {//for local testings and visibility
            driver.manage().window().maximize();
        }

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //enabling downloading resources when driver is headless
        enableHeadlessDownload(service, driver, downloadsFolder);

        return new DriverFactory(driver);
    }

    private static void enableHeadlessDownload(ChromeDriverService service, ChromeDriver driver, File downloadsFolder) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            Map<String, Object> commandParams = new HashMap<>();
            commandParams.put("cmd", "Page.setDownloadBehavior");
            Map<String, String> params = new HashMap<>();
            params.put("behavior", "allow");
            params.put("downloadPath", downloadsFolder.getAbsolutePath());
            commandParams.put("params", params);
            ObjectMapper objectMapper = new ObjectMapper();
            String command = objectMapper.writeValueAsString(commandParams);
            String u = service.getUrl().toString() + "/session/" + driver.getSessionId() + "/chromium/send_command";
            HttpPost request = new HttpPost(u);
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity(command));
            CloseableHttpResponse response = httpClient.execute(request);
            Log.info(String.format("enable download, status code=[%d]", response.getCode()));
        } catch (Exception e) {
            Log.error("failed to send command=[age.setDownloadBehavior] to chrome server");
            Log.error(e.getMessage());
        }
    }

    public boolean isClickable(By by, Duration duration) {
        try {
            waitForElmClickable(duration, by);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public boolean isVisible(By by, Duration duration) {
        try {
            waitForElmVisibility(duration, by);
            return true;
        } catch (WebDriverException e) {
            return false;
        }
    }

    public void waitForPageCompletelyLoad() {
        new WebDriverWait(driver, Duration.TWO_SECONDS.getValue()).until(
                webDriver -> executeScript("return document.readyState").equals("complete"));
    }

    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public String getAttribute(By by, String attributeKey) {
        return waitForElmVisibility(WAIT_ELEMENT_TIMEOUT, by).getAttribute(attributeKey);
    }

    public boolean waitForElmContains(WebElement element, String text) {
        return waitForElmContains(WAIT_ELEMENT_TIMEOUT, element, text);
    }

    public boolean waitForElmContains(Duration duration, WebElement element, String text) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
            return wait.until(ExpectedConditions.textToBePresentInElement(element, text));
        } catch (TimeoutException e) {
            Log.error(
                    String.format("failed waiting to locator=[%s] text=[%s] for timeout [%s] secs", element, text, duration));
            return false;
        }
    }

    public static boolean compareTextInput(WebElement inputElm, String text) {
        String inputContent = inputElm.getAttribute("value");
        return text.equals(inputContent);
    }

    public void setInput(WebElement inputElm, String text) {
        executeScript("arguments[0].value='" + text + "';", inputElm);
    }

    public void scrollByDesiredSize(String size) {
        executeScript(String.format("window.scrollBy(0, %s)", size), "");
    }

    public void clickByScript(WebElement element) {
        executeScript("arguments[0].click", element);
    }

    public void scrollToViewScript(WebElement element) {
        executeScript("arguments[0].scrollIntoView();", element);
    }

    public void scrollIntoTheMiddleOfPage(WebElement elem) {
        executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'})", elem);
    }

    public static boolean isChildElmVisible(WebElement parentElm, By childBy) {
        try {
            return parentElm.findElement(childBy).isEnabled();
        } catch (NoSuchElementException e) {
            Log.info("child element don't exist", e);
            return false;
        }
    }

    public WebElement waitForElmVisibility(Duration duration, By by) {
        WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public WebElement waitForElmClickable(Duration duration, By by) {
        WebDriverWait wait = new WebDriverWait(driver, duration.getTimeUnit().toSeconds(duration.getValue()));
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    public void mouseOver(WebElement targetElm) {
        actions = new Actions(driver);
        actions.moveToElement(targetElm).build().perform();
    }

    public void doubleClick(WebElement actionElm) {
        actions = new Actions(getWebDriver());
        actions.doubleClick(actionElm).build().perform();
    }

    public void dragAndDrop(WebElement source, WebElement target) {
        actions = new Actions(getWebDriver());
        actions.dragAndDrop(source, target).build().perform();
    }

    public void clickByOffset(int xOffset, int yOffset) {
        actions = new Actions(driver);
        actions.moveByOffset(xOffset, yOffset).click().build().perform();
    }

    @Override
    public void get(String s) {
        driver.get(s);
    }

    @Override
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return driver.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    @Override
    public String getPageSource() {
        return driver.getPageSource();
    }

    @Override
    public void close() {
        driver.close();
    }

    @Override
    public void quit() {
        driver.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return driver.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return driver.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        return driver.switchTo();
    }

    @Override
    public Navigation navigate() {
        return driver.navigate();
    }

    @Override
    public Options manage() {
        return driver.manage();
    }

    public void scrollToEndOfPage() {
        executeScript("window.scrollTo(0, document.body.scrollHeight)");
    }

    public void scrollToTopOfPage() {
        driver.findElement(By.cssSelector("body")).sendKeys(Keys.CONTROL, Keys.HOME);
    }

    public void switchToFrame() {
        driver.switchTo().frame(findElement(By.cssSelector(".iframe")));
    }

    public void switchToMainWorkingWindow() {
        driver.switchTo().parentFrame();
    }

    public WebDriver getWebDriver() {
        return driver;
    }
}
