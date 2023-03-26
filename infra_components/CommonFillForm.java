package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CommonFillForm extends PageElements{
    @FindBy(css = "input[cywapp-id='name']")
    public WebElement fullNameElm;
    @FindBy(css = "input[placeholder='Phone']")
    public WebElement phoneNumberElm;
    @FindBy(css = "input[type='email']")
    public WebElement emailElm;
    @FindBy(css = "input[cywapp-id='password']")
    public WebElement newPasswordElm;
    @FindBy(css = " input[placeholder='Old Password']")
    public WebElement oldPasswordElm;
    @FindBy(css = "button[type='submit']")
    public WebElement submitBtnElm;
    public CommonFillForm(DriverFactory driver) {
        super(driver);
    }

    private CommonFillForm setFullName(String fullName) {
        clearAndSetText(fullNameElm, fullName);
        return this;
    }

    private CommonFillForm setPhoneNumber(String phoneNumber) {
        clearAndSetText(phoneNumberElm, phoneNumber);
        return this;
    }

    private CommonFillForm setNewPassword(String newPassword) {
        clearAndSetText(newPasswordElm, newPassword);
        return this;
    }

    private CommonFillForm setOldPassword(String oldPassword) {
        clearAndSetText(oldPasswordElm, oldPassword);
        return this;
    }

    private CommonFillForm setEmail(String email) {
        clearAndSetText(emailElm, email);
        return this;
    }

    public void clickSubmitBtn(){
        clickElm(submitBtnElm);
    }

    public void fillEditProfileDetails(String fullName, String email, String phone, String oldPassword, String newPassword){
        setCommonDetails(fullName, email,phone,newPassword)
        .setOldPassword(oldPassword);
        clickSubmitBtn();
    }

    public CommonFillForm setCommonDetails(String fullName, String email, String phone, String newPassword) {
        setFullName(fullName).
                setPhoneNumber(phone).
                setEmail(email).
                setNewPassword(newPassword);
        return this;
    }

}
