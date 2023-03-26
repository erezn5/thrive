package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.selenium.DriverFactory;
import com.hackeruso.automation.utils.FileUtil;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.support.PageFactory;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public abstract class BasePage extends PageElements {

    private String url;

    protected final static String URL_ADDRESS = EnvConf.getProperty("base.url");
    private final static String log4jConfigFile = FileUtil.getFile("/morpheus/src/main/resources/" + EnvConf.getProperty("conf.log4j"));

    public BasePage(DriverFactory driver, String path) {
        super(driver);
        this.url = URL_ADDRESS + "/" + path;
        PageFactory.initElements(driver, this);
        PropertyConfigurator.configure(log4jConfigFile);
    }

    public BasePage(DriverFactory driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        PropertyConfigurator.configure(log4jConfigFile);
    }

    private void navigate() {
        driver.get(url);
        waitForPageCompletelyLoad();
        Log.info(String.format("navigate to web url=[%s]", url));
    }

    public abstract boolean verifyElement();

    public void navigateAndVerify() {
        navigate();
        if (verifyElement()) {
            Log.info("Navigation succeeded!!");
        }
    }

    public void refresh() {
        Log.info(String.format("refresh url '%s'", driver.getCurrentUrl()));
        driver.navigate().refresh();
    }
}
