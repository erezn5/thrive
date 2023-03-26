package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.awaitility.Duration;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;


public class ConfirmPopUp extends ConfirmationModal {
    @FindBy(css = "button[cywapp-id='btn-accept']")
    private WebElement yesBtnElm;
    @FindBy(css = "button[cywapp-id='btn-cancel']")
    private WebElement noBthElm;

    public ConfirmPopUp(DriverFactory driver) {
        super(driver);
    }

    public ConfirmPopUp(DriverFactory driver, WebElement yesBtnElm, WebElement noBthElm){
        super(driver);
        this.yesBtnElm = yesBtnElm;
        this.noBthElm = noBthElm;
    }
    @Override
    public void clickNo() {
        if(isElementPresent(noBthElm)) {
            verifyElm(noBthElm);
            clickElm(noBthElm);
        }
    }

    @Override
    public void clickAccept(){
        if(isElementPresent(yesBtnElm)) {
            sleep(Duration.ONE_SECOND);
            clickElm(yesBtnElm);
        }
    }

    public void acceptOrNot(boolean acceptFlag){
        if(acceptFlag){
            clickAccept();
        }else{
            clickNo();
        }
    }

    public boolean verifyConfirmPopUpElement() {
        sleep(Duration.ONE_SECOND);
        return isElmExist(yesBtnElm);
    }
}
