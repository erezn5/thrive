package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public abstract class AbstractCheckBox extends PageElements {

    private By checkBoxBy;

    protected AbstractCheckBox(DriverFactory driver, By checkBoxBy) {
        super(driver);
        this.checkBoxBy = checkBoxBy;
    }

    protected AbstractCheckBox(DriverFactory driver){
        super(driver);
    }

    protected abstract boolean isChecked();

    public void check(WebElement checkBoxElm, boolean check){
        if(isChecked() ^ check){
            clickElm(checkBoxElm);
        }
    }

    public void check(boolean check){
        if(isChecked() ^ check){
            clickOnEnabledButton(checkBoxBy);
        }
    }
}
