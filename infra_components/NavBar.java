package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.awaitility.Duration;
import org.openqa.selenium.By;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NavBar<ITEM extends Enum<ITEM>> extends PageElements {

    private final Map<Enum<ITEM>, By> navBarItemsMap;

    public NavBar(DriverFactory driver, Map<Enum<ITEM>, By> navBarItemsMap) {
        super(driver);
        this.navBarItemsMap = navBarItemsMap;
    }

    public void clickItem(Enum item) {
        clickButtonWithDuration(getItemByEnum(item), new Duration(5, TimeUnit.SECONDS));
        sleep(Duration.ONE_SECOND);//FIXME - AUT-77
    }

    public boolean isItemExist(Enum item) {
        return isVisible(getItemByEnum(item), Duration.TWO_SECONDS);
    }

    private By getItemByEnum(Enum item) {
        return navBarItemsMap.get(item);
    }
}
