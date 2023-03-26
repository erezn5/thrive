package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.awaitility.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

@Deprecated
public abstract class CommonDropDownMenu extends PageElements {
    //we will remove this class and use the new created class - DropDownMenu
    private final WebElement dropDownMenuBthElm;
    private final ConfirmPopUp confirmPopUp;
    public CommonDropDownMenu(DriverFactory driver, WebElement dropDownMenuBthElm) {
        super(driver);
        this.dropDownMenuBthElm = dropDownMenuBthElm;
        confirmPopUp = new ConfirmPopUp(driver);
    }

    private void openDropDownMenu() {
        clickElmRetry(dropDownMenuBthElm);//TODO - Check if to revert back to clickElmRetry
    }

    public void selectItemFromDropDownMenuWithNoPopup(String item){
        openDropDownMenu();
        sleep(Duration.ONE_SECOND);
        clickElm(createElmFromCss(item));
    }

    public void selectItemFromDropDownMenu(String item) {
        openDropDownMenu();
        verifyWithPopupWindow();
        clickOnEnabledButton(createSelector(item));
    }

    private void verifyWithPopupWindow() {
        if (confirmPopUp.verifyConfirmPopUpElement()) {
            confirmPopUp.clickAccept();
            Log.info("Clicking on accept button on popup window is successful");
        }
    }

    public void selectItemFromDropDownMenuWithName(WebElement inputElm, String name) {
        if (!name.equals("")) {
            openDropDownMenu();
            sleep(Duration.ONE_SECOND);//FIXME AUT-77
            setText(inputElm, name);
            if (isVisible(createSelector(String.format("[cywapp-id*='%s']", name.toLowerCase())), Duration.ONE_SECOND)) {
                clickElm(createElmFromCss(String.format("[cywapp-id*='%s']", name.toLowerCase())));
            }
        }
    }

    protected abstract By createSelector(String item);

}