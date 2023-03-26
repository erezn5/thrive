package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.selenium.DriverFactory;
import com.hackeruso.automation.utils.Waiter;
import org.apache.commons.lang.StringUtils;
import org.awaitility.Duration;
import org.awaitility.core.Condition;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class PageElements {

    protected static final Duration WAIT_TIMEOUT = new Duration(EnvConf.getAsInteger("ui.locator.timeout.sec"), TimeUnit.SECONDS);
    protected static final int CONDITION_RETRY = EnvConf.getAsInteger("ui.locator.action.retry");
    protected static final String UPLOAD_PERCENTAGE = "100%";

    protected final DriverFactory driver;

    protected PageElements(DriverFactory driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public String getElementAttributeItem(WebElement elem, String item) {
        return elem.getAttribute(item);
    }

    public boolean verifyByAttributeItemContainsText(By searchBy, String item, String containText) {
        return driver.getAttribute(searchBy, item).contains(containText);
    }

    protected boolean verifyElementAttributeItemContainsText(WebElement elm, String item, String text) {
        return getElementAttributeItem(elm, item).contains(text);
    }

    protected void clearTextFromSearchInputText(WebElement inputElm) {
        if (isElmExist(inputElm)) {
            inputElm.click();
            inputElm.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
        }
    }

    public void waitForPageCompletelyLoad() {
        driver.waitForPageCompletelyLoad();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    protected void clickButton(By byBth) {
        clickButtonWithDuration(byBth, WAIT_TIMEOUT);
    }

    protected void clickButtonWithDuration(By byBth, Duration timeout) {
        if (isClickableWithDuration(byBth, timeout)) {
            WebElement bthElem = driver.findElement(byBth);
            clickElm(bthElem);
        }
    }

    private WebElement waitForClickableElm(By by, Duration timeout) {
        WebElement element = driver.waitForElmClickable(timeout, by);
        printClickableElm(by);
        return element;
    }

    protected WebElement waitForClickableElm(By by) {
        return waitForClickableElm(by, WAIT_TIMEOUT);
    }

    protected WebElement waitForClickableElmWithDuration(By by, Duration duration) {
        return waitForClickableElm(by, duration);
    }

    public PageElements clickElm(WebElement btnElem) {
        btnElem.click();
        printElm(String.format("click on '%s' button", btnElem));
        return this;
    }

    public void clickElmByValue(String cssValue) {
        WebElement elem = createElmFromCss(cssValue);
        elem.click();
        printElm(String.format("click on '%s' button", elem));
    }

    protected void clickElmRetry(final WebElement bthElm) {
        Condition<Boolean> condition = () -> {
            try {
                clickElm(bthElm);
                Log.i("button=[%s] clicked successfully", bthElm);
                return true;
            } catch (Exception e) {
                Log.debug(String.format("failed to click on button=[%s]", bthElm), e);
                return false;
            }
        };

        Boolean success = Waiter.waitCondition(WAIT_TIMEOUT, condition);
        throwElmNotClickable(success, bthElm);
    }

    protected void clickElmRetry(final WebElement bthElm, Duration duration) {
        Condition<Boolean> condition = () -> {
            try {
                clickElm(bthElm);
                Log.i("button=[%s] clicked successfully", bthElm);
                return true;
            } catch (Exception e) {
                Log.debug(String.format("failed to click on button=[%s]", bthElm), e);
                return false;
            }
        };

        Boolean success = Waiter.waitCondition(duration, condition);
        throwElmNotClickable(success, bthElm);
    }

    protected boolean verifyElmText(WebElement element, String text) {
        printElm(String.format("about to verify text in element '%s' with the text: '%s'", element, text));
        return waitForElmContainsTextWithDuration(element, text, Duration.FIVE_SECONDS);
    }

    public void printElm(String message) {
        Log.info(message);
    }

    public PageElements clearAndSetText(WebElement inputElm, String text) {
        clearTextFromSearchInputText(inputElm);

        if (text == null) return this;

        inputElm.sendKeys(text);
        printSet(inputElm, text);
        return this;
    }

    public void waitForElmToDisappear(WebElement elm) {
        waitForElmToDisappearWithDuration(elm, Duration.TWO_SECONDS);
    }

    public void waitForElmToDisappearWithDuration(WebElement elm, Duration duration) {
        Condition<Boolean> condition = () -> !isElmExist(elm);
        Waiter.waitCondition(duration, condition, Duration.TWO_SECONDS);
    }

    public void clearAndSetTextAsNumber(WebElement inputElm, String text) {
        verifyElm(inputElm);
        clearTextFromSearchInputText(inputElm);
        inputElm.sendKeys(String.valueOf(text));
        printSet(inputElm, text);
    }

    private void printSet(WebElement txtElm, String txt) {
        Log.i("set '%s' with value '%s'", txtElm, txt);
    }

    protected WebElement getElmParent(WebElement elm) {
        return elm.findElement(By.xpath(".."));
    }

    protected void verifyAndClickElm(WebElement elm) {
        if (verifyElm(elm)) {
            clickElmRetry(elm);
        }
    }

    protected boolean verifyElm(WebElement verifierElm) {
        Condition<Boolean> condition = verifierElm::isDisplayed;
        return Waiter.waitCondition(new Duration(30, TimeUnit.SECONDS), condition, Duration.FIVE_SECONDS);
    }

    protected boolean verifyElmEnabled(WebElement verifierElm) {
        Condition<Boolean> condition = verifierElm::isEnabled;
        return Waiter.waitCondition(new Duration(30, TimeUnit.SECONDS), condition, Duration.FIVE_HUNDRED_MILLISECONDS);
    }

    protected boolean verifyStringInElmList(List<WebElement> elementList, String value) {
        Log.info(String.format("verify the value of: '%s' in list of elements", value));
        Condition<Boolean> condition = () -> elementList.stream().anyMatch(element -> getText(element).toUpperCase().contains(value.toUpperCase()));
        return Waiter.waitCondition(Duration.FIVE_SECONDS, condition, Duration.FIVE_HUNDRED_MILLISECONDS);
    }

    protected String getText(WebElement elm) {
        String text = "";

        if (elm == null) {
            return text;
        }

        try {
            text = elm.getText();

            if (StringUtils.isEmpty(text)) {
                text = elm.getAttribute("value");
            }
            if (StringUtils.isEmpty(text)) {
                text = elm.getAttribute("innerHtml");
            }
            if (StringUtils.isEmpty(text)) {
                text = elm.getAttribute("innerText");
            }
        } catch (Exception e) {
            Log.i("Error reading text", e);
        }
        return text;
    }

    public void clickOnElmFromList(List<WebElement> elmList, String text) {
        Boolean clicked = clickOnElmFromListAndVerify(elmList, text);
        throwElmNotClickable(clicked, text);
    }

    private Condition<Boolean> getBooleanCondition(List<WebElement> elmList, String text) {
        return () -> {
            try {
                verifyStringInElmList(elmList, text);
                WebElement elem = Objects.requireNonNull(elmList.stream().filter(elm -> getText(elm).contains(text)).findFirst().orElse(null));
                clickElm(elem);
                Log.info(String.format("Button=[%s] clicked successfully", text));
                return true;
            } catch (Exception e) {
                Log.debug(e.getMessage());
                return false;
            }
        };
    }

    public boolean clickOnElmFromListAndVerify(List<WebElement> elmList, String text) {
        Condition<Boolean> condition = getBooleanCondition(elmList, text);
        return Waiter.waitCondition(Duration.TEN_SECONDS, condition);
    }

    public WebElement getElmFromListContainsText(List<WebElement> elmList, String text) {
        return Objects.requireNonNull(elmList.stream().filter(elm -> getText(elm).contains(text)).findFirst().orElse(null));
    }

    public WebElement getChildElmFromParentBy(WebElement element, By by) {
        return element.findElement(by);
    }

    public boolean clickOnElmParentFromListAndVerify(List<WebElement> elmList, String text) {
        Condition<Boolean> condition = () -> {
            try {
                WebElement elem = getElmFromListContainsText(elmList, text);
                if (getText(elem).contains(text)) {
                    clickElm(getElmParent(elem));
                    return true;
                }
            } catch (NullPointerException e) {
                Log.i("cannot click on item from list with the following error=[%s]", e.getMessage());
            }
            return false;
        };
        return Waiter.waitCondition(Duration.TEN_SECONDS, condition);
    }

    public void clickOnElmParentFromList(List<WebElement> elmList, String text) {
        Condition<Boolean> condition = () -> {
            WebElement elem = getElmFromListContainsText(elmList, text);
            if (getText(elem).contains(text)) {
                clickElm(getElmParent(elem));
                return true;
            }
            return false;
        };
        Boolean clicked = Waiter.waitCondition(Duration.TEN_SECONDS, condition);
        throwElmNotClickable(clicked, text);
    }

    protected static void throwElmNotClickable(Boolean clicked, WebElement bthElm) {
        if (clicked == null || !clicked) {
            throw new ElementClickInterceptedException(String.format("failed to click on bth element=[%s]", bthElm));
        }
    }

    protected static void throwElmNotClickable(Boolean clicked, String locator) {
        if (clicked == null || !clicked) {
            throw new ElementClickInterceptedException("failed to click on bth locator=[" + locator + "]");
        }
    }

    protected void clickOnEnabledButton(By bthBy) {
        clickOnEnabledButton(bthBy, new Duration(7, TimeUnit.SECONDS));
    }

    private void clickOnEnabledButton(By bthBy, Duration duration) {
        WebElement bthElem = waitForElementWithDuration(bthBy, duration);
        clickElm(bthElem);
    }

    private void printClickableElm(By by) {
        Log.info(String.format("locator=[%s] is clickable", by.toString()));
    }

    // NOTICE: don't use it only if you must to!
    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.getTimeUnit().toMillis(duration.getValue()));
        } catch (InterruptedException e) {
            Log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected WebElement waitForElement(By by) {
        return waitForElementWithDuration(by, WAIT_TIMEOUT);
    }

    protected WebElement waitForElementWithDuration(By by, Duration duration) {
        WebElement element = driver.waitForElmVisibility(duration, by);
        printElmVisibility(by, (element != null));
        return element;
    }

    protected void waitForElmVisibility(By by) {
        try {
            waitForElement(by);
        } catch (Exception ignore) {
            Log.w(String.format("Locator=[%s] not found", by));
        }
    }

    private void printElmVisibility(By by, boolean appear) {
        Log.debug(String.format("locator=[%s] visible=[%b]", by.toString(), appear));
    }

    protected void setText(WebElement txtElm, String text) {
        txtElm.sendKeys(text);
        printSet(txtElm, text);
    }

    private boolean setTextAndValidate(WebElement inputElm, String text) {
        clearAndSetText(inputElm, text);
        return DriverFactory.compareTextInput(inputElm, text);
    }

    protected static void throwElmNotFound(boolean notFound, String locator) {
        if (notFound) {
            throw new NoSuchElementException("failed to find locator=[" + locator + "]");
        }
    }

    protected boolean waitForTextElmVisibilityByElem(WebElement element, String text) {
        Condition<Boolean> condition = () -> {
            try {
                return waitForElmContainsText(element, text);
            } catch (Exception e) {
                Log.debug("failed to locate label " + text, e);
                return false;
            }
        };

        return Waiter.waitCondition(WAIT_TIMEOUT, condition);
    }

    protected boolean waitForTextElmVisibilityByElemWithDuration(Duration duration, WebElement elem, String text) {
        return driver.waitForElmContains(duration, elem, text);
    }

    private boolean waitForElmContainsTextWithDuration(WebElement element, String text, Duration duration) {
        boolean contains = driver.waitForElmContains(duration, element, text);
        return commonWaitForElmContainsTextCondition(element, text, contains);
    }

    private boolean waitForElmContainsText(WebElement element, String text) {
        boolean contains = driver.waitForElmContains(element, text);
        return commonWaitForElmContainsTextCondition(element, text, contains);
    }

    private boolean commonWaitForElmContainsTextCondition(WebElement element, String text, boolean contains) {
        if (contains) {
            printSet(element, text);
        } else {
            printElmTextNotFound(element, text);
        }
        return contains;
    }

    public void mouseOver(WebElement target) {
        sleep(Duration.FIVE_HUNDRED_MILLISECONDS);
        isElmExist(target);
        driver.mouseOver(target);
    }

    private void printElmTextNotFound(WebElement element, String content) {
        Log.info(String.format("element=[%s] NOT contains text=[%s]", element, content));
    }

    protected boolean isElmExist(WebElement elm) {
        try {
            elm.isDisplayed();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    protected boolean isElementPresentWithDuration(WebElement elem, Duration duration) {
        Condition<Boolean> condition = () -> {
            try {
                isDisplayedElm(elem);
            } catch (NoSuchElementException | NullPointerException e) {
                Log.error(e.getMessage());
                return false;
            }
            return true;
        };
        return Waiter.waitCondition(new Duration(duration.getValueInMS(), TimeUnit.MILLISECONDS), condition);
    }

    protected boolean isElementPresent(WebElement webElem) {
        Condition<Boolean> condition = () -> {
            try {
                return isDisplayedElm(webElem);
            } catch (NoSuchElementException | NullPointerException e) {
                Log.error(e.getMessage());
                return false;
            }
        };
        return Waiter.waitCondition(new Duration(WAIT_TIMEOUT.getValue(), TimeUnit.SECONDS), condition);
    }

    protected void setTextRetry(final WebElement inputElm, final String text) {
        throwElmNotFound(inputElm == null, "contains  text=" + text);
        Condition<Boolean> condition = () -> {
            try {
                boolean success = setTextAndValidate(inputElm, text);
                if (!success) {
                    driver.setInput(inputElm, text);
                    success = DriverFactory.compareTextInput(inputElm, text);
                }
                return success;
            } catch (Exception e) {
                Log.error(e.getMessage());
                return false;
            }
        };
        Boolean success = Waiter.waitCondition(new Duration(WAIT_TIMEOUT.getValue(), TimeUnit.SECONDS), condition);
        throwChangeElementStateException(success, inputElm.getAttribute("class"), text);
    }

    public void switchToMainPageById(String mainChallengePage) {
        driver.switchTo().window(mainChallengePage);
    }

    public static class ChangeElementStateException extends RuntimeException {
        ChangeElementStateException(String message) {
            super(message);
        }
    }

    protected static void throwChangeElementStateException(Boolean success, String locator, String value) {
        if (success == null || !success) {
            throw new ChangeElementStateException(String.format("failed to set locator=[%s] with value=[%s]", locator, value));
        }
    }

    protected WebElement createElmFromByLocator(By by) {
        return driver.findElement(by);
    }

    protected boolean isClickable(By by) {
        return waitForClickableElm(by, WAIT_TIMEOUT) != null;
    }

    protected boolean isClickableWithDuration(By by, Duration duration) {
        boolean clickable = driver.isClickable(by, duration);
        Log.debug(String.format("locator=[%s] isClickable=[%b]", by, clickable));
        return clickable;
    }

    protected boolean isVisible(By by, Duration duration) {
        boolean visible = driver.isVisible(by, duration);
        printElmVisibility(by, visible);
        return visible;
    }

    protected boolean isVisible(By by) {
        return isVisible(by, new Duration(CONDITION_RETRY, TimeUnit.SECONDS));
    }

    protected boolean isVisibleWithDuration(By by, Duration duration) {
        return isVisible(by, duration);
    }

    protected void clickIfVisible(By bthBy) {
        if (isVisible(bthBy)) {
            clickButton(bthBy);
        } else {
            Log.info(String.format("locator '%s' not visible, skip on click", bthBy));
        }
    }

    protected boolean isDisplayedElm(WebElement elm) {
        return elm.isDisplayed();
    }

    protected boolean isEnabledElm(WebElement elm) {
        return elm.isEnabled();
    }

    public WebElement createElmFromCss(String text) {
        try {
            return createElmFromByLocator(By.cssSelector(text));
        } catch (NullPointerException | NoSuchElementException e) {
            Log.e("Not found element for =[%s] text", text);
        }

        return null;
    }

    public List<WebElement> findElementsListFromCss(String cssSelectorTxt) {
        return driver.findElements(By.cssSelector(cssSelectorTxt));
    }

    public By createByFromCss(String text) {
        return By.cssSelector(text);
    }

    public By createByFromXpath(String text) {
        return By.xpath(text);
    }

    public WebElement createXpathElem(String text) {
        return driver.findElement(By.xpath(text));
    }

    public List<WebElement> getWebElementsListFromCssSelector(String cssSelectorStr) {
        waitForElement(By.cssSelector(cssSelectorStr));
        return driver.findElements(By.cssSelector(cssSelectorStr));
    }

    public void clickOnItemFromList(List<WebElement> elementList, String text) {
        if (elementList != null)
            for (WebElement name : elementList)
                if (getText(name).toLowerCase().contains(text.toLowerCase())) {
                    driver.scrollToViewScript(name);
                    if (!isElementPresent(name)) {
                        sleep(Duration.TWO_SECONDS);
                    }
                    clickElm(name);
                    break;
                }
    }

    public void scrollByDesiredSize(String size) {
        driver.scrollByDesiredSize(size);
    }

    public void scrollToItemInTheList(List<WebElement> elementList, String text) {
        if (elementList != null)
            for (WebElement name : elementList)
                if (getText(name).toLowerCase().contains(text.toLowerCase())) {
                    driver.scrollToViewScript(name);
                    break;
                }
    }

    public boolean verifyAndSelectItemInTheDropDownMenu(List<WebElement> elementList, String itemToSearch) {
        for (WebElement element : elementList) {
            scrollElmIntoView(element);
            if (getText(element).equals(itemToSearch)) {
                clickElm(element);
                return true;
            }
        }
        return false;
    }

    public void scrollAndSelectItemInDropDownMenuDynamically(String listByStr, String itemToSearch) {
        List<WebElement> elementList = getWebElementsListFromCssSelector(listByStr);
        for (int i = 0; i < elementList.size(); i++) {
            scrollElmIntoView(elementList.get(i));
            if (getText(elementList.get(i)).equalsIgnoreCase(itemToSearch)) {
                verifyElm(elementList.get(i));
                clickElm(elementList.get(i));
                return;
            }
            elementList = getWebElementsListFromCssSelector(listByStr);
            if (i == elementList.size() - 1) {
                elementList = getWebElementsListFromCssSelector(listByStr);
            }
        }

    }

    protected void scrollElmIntoView(WebElement elem) {
        driver.scrollToViewScript(elem);
    }

    protected void scrollIntoTheMiddleOfPage(WebElement elem) {
        driver.scrollIntoTheMiddleOfPage(elem);
    }

    protected void scrollDownToEndOfPage() {
        driver.scrollToEndOfPage();
    }

    protected void scrollToTopOfPage() {
        driver.scrollToTopOfPage();
    }

    public void clickByOffset(int xOffset, int yOffset) {
        driver.clickByOffset(xOffset, yOffset);
    }

    protected void uploadResource(File file, WebElement fileInputElm) throws FileNotFoundException {
        if (file.exists()) {
            handleFileUpload(file, fileInputElm);
        } else {
            throw new FileNotFoundException("File didn't found at: " + file.getAbsolutePath());
        }
    }

    private void handleFileUpload(File file, WebElement fileInputElm) {
        sleep(Duration.TWO_SECONDS);//FIXME AUT-77
        setText(fileInputElm, file.getAbsolutePath());
    }

    public String getCurrentURL() {
        return driver.getCurrentUrl();
    }

    public boolean verifyTextHasText(String completeText, String targetText) {
        if (completeText.contains(targetText)) {
            Log.i("Verify [%s] successfully contains [%s]", completeText, targetText);
            return true;
        } else {
            Log.i("Fail to verify [%s] contains [%s]", completeText, targetText);
            return false;
        }
    }

    public String getCurrentWindowTab() {
        return driver.getWindowHandle();
    }

    public void switchBrowserWindow() {
        String homeWindow = getCurrentWindowTab();
        Set<String> allWindows = driver.getWindowHandles();

        for (String handle : allWindows) {
            if (!handle.equals(homeWindow)) {
                driver.switchTo().window(handle);
            }
        }
    }

    public void switchToPageAndCloseOtherPages(String toPage) {
        Set<String> allWindows = driver.getWindowHandles();

        for (String handle : allWindows) {
            if (!handle.equals(toPage)) {
                driver.switchTo().window(handle);
                driver.close();
            }
        }
        switchToMainPageById(toPage);
    }

    public boolean isElementPresent(By locatorKey) {
        try {
            driver.findElement(locatorKey);
            return true;
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public int getTotalCountOfItemInText(String text) {
        return Integer.parseInt(text.split("\\(")[1].split("\\)")[0]);
    }

    public void switchToFrame() {
        sleep(Duration.ONE_SECOND);
        driver.switchToFrame();
    }

    public void switchToMainWorkingWindow() {
        sleep(Duration.ONE_SECOND);
        driver.switchToMainWorkingWindow();
    }

    public void waitUploadIsFinished(WebElement uploadingPercentageElm) {
        waitForTextElmVisibilityByElem(uploadingPercentageElm, UPLOAD_PERCENTAGE);
    }

    protected void doubleClick(WebElement actionElm) {
        driver.doubleClick(actionElm);
    }

    protected void dragAndDrop(WebElement source, WebElement target) {
        driver.dragAndDrop(source, target);
    }
}
