package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;

public abstract class ConfirmationModal extends PageElements {

    protected ConfirmationModal(DriverFactory driver) {
        super(driver);
    }

    public abstract void clickNo();

    public abstract void clickAccept();


}