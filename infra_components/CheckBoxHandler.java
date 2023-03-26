package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.openqa.selenium.By;

public class CheckBoxHandler extends AbstractCheckBox {

    private final By checkboxBy;

    public CheckBoxHandler(DriverFactory driver, By checkboxBy){
        super(driver, checkboxBy);
        this.checkboxBy = checkboxBy;
    }

    @Override
    protected boolean isChecked() {
        return verifyByAttributeItemContainsText(checkboxBy, "class","v-input--is-label-active");
    }
}
