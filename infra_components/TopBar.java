package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.model.pages.website.profile.ProfilePage;
import com.hackeruso.automation.selenium.DriverFactory;
import org.awaitility.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.Map;

public class TopBar extends NavBar<TopNavBarItem> {
    private final ProfileDetailsArea profileDetailsArea;

    private static final By homeItemBy = By.cssSelector("li[class*='toolbar-logo'] img");//li[cywapp-id='home']
    private static final By challengesItemBy = By.cssSelector(("li[cywapp-id='challenges']"));
    private static final By practiceArenaItemBy = By.cssSelector(("li[cywapp-id='practice_arena']"));
    private static final By leaderboardItemBy = By.cssSelector(("[cywapp-id='leaderboard']"));
    private static final By cyberpediaItemBy = By.cssSelector(("li[cywapp-id='cyberpedia']"));
    private static final By cmsItemBy = By.cssSelector(("[cywapp-id='CMS']"));
    private static final By profileItemBy = By.cssSelector("[cywapp-id='profile']");

    public TopBar(DriverFactory driver){
        super(driver, createMenuItemsMap());
        profileDetailsArea = new ProfileDetailsArea(driver);
    }

    private static Map<Enum<TopNavBarItem>, By> createMenuItemsMap(){
        Map<Enum<TopNavBarItem>, By> itemsMap = new HashMap<>();
        itemsMap.put(TopNavBarItem.HOME, homeItemBy);
        itemsMap.put(TopNavBarItem.CHALLENGES, challengesItemBy);
        itemsMap.put(TopNavBarItem.PRACTICE_ARENA, practiceArenaItemBy);
        itemsMap.put(TopNavBarItem.LEADERBOARD, leaderboardItemBy);
        itemsMap.put(TopNavBarItem.CYBERPEDIA, cyberpediaItemBy);
        itemsMap.put(TopNavBarItem.CMS, cmsItemBy);
        itemsMap.put(TopNavBarItem.PROFILE_NAME, profileItemBy);
        return itemsMap;
    }

    public String getUserTotalPoints(){
        profileDetailsArea.openProfileDropDownMenu();
        return profileDetailsArea.getTotalPoints();
    }

    public void clickWebsiteLogout(){
        if(profileDetailsArea.verifyProfileStatus()) {
            profileDetailsArea.openProfileDropDownMenu();
            profileDetailsArea.clickLogout();
            sleep(Duration.ONE_SECOND);
        }
    }

    public ProfilePage clickProfilePage(){
        profileDetailsArea.openProfileDropDownMenu();
        return profileDetailsArea.clickProfilePage();
    }

    private static class ProfileDetailsArea extends PageElements {
        @FindBy(css = "[cywapp-id='profile']")
        private WebElement hiddenProfileElm;
        @FindBy(css = "div[id='profile_side_box'] p:nth-child(1)")//FIXME-TDP-1857
        private WebElement userPointsElm;
        @FindBy(css = "div[id='profile_side_box'] div:nth-child(3) button")//FIXME-TDP-1857
        private WebElement profileBtnElm;
        @FindBy(css = "button[class*='btn-logout']")//FIXME-TDP-1857
        private WebElement logoutElm;
        @FindBy(css = "button[class*='btn-support']")//FIXME-TDP-1857
        private WebElement supportElm;
        protected ProfileDetailsArea(DriverFactory driver) {
            super(driver);
            menu = new ProfileDropDownMenu(driver);
        }

        ProfileDropDownMenu menu;

        public void openProfileDropDownMenu(){
            if(!menu.isChecked()){
                if(isElementPresentWithDuration(hiddenProfileElm, Duration.TEN_SECONDS)) {
                    clickElm(hiddenProfileElm);
                }
            }
        }

        public ProfilePage clickProfilePage(){
            clickElm(profileBtnElm);
            return new ProfilePage(driver);
        }

        public void clickLogout(){
            if(isElmExist(logoutElm)){
                verifyAndClickElm(logoutElm);
            }else{
                System.out.println("NOT FOUND");
            }
        }

        public String getTotalPoints() {
            return getText(userPointsElm).split(":")[1].split("/")[0].trim();
        }

        public boolean verifyProfileStatus() {
            return isElementPresent(hiddenProfileElm);
        }

        static class ProfileDropDownMenu extends AbstractCheckBox{
            protected ProfileDropDownMenu(DriverFactory driver) {
                super(driver);
            }

            @Override
            protected boolean isChecked() {
                return verifyByAttributeItemContainsText(profileItemBy, "class", "profile-text-active");
            }
        }
    }
}
