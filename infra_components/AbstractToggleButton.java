package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.openqa.selenium.WebElement;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public abstract class AbstractToggleButton extends PageElements {

    protected final WebElement leftButton;
    protected final WebElement rightButton;

    protected AbstractToggleButton(DriverFactory driver, WebElement leftButton, WebElement rightButton) {
        super(driver);
        this.leftButton = leftButton;
        this.rightButton = rightButton;
    }

    protected abstract boolean isLeftPressed();

    public void switchLeft() {
        if (isLeftPressed()) {
            Log.info("left toggle button already pressed");
        } else {
            clickElm(leftButton);
        }
    }

    public void switchRight() {
        if (isLeftPressed()) {
            clickElm(rightButton);
        } else {
            Log.info("right toggle button already pressed");
        }
    }
}
