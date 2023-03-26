package com.hackeruso.automation.model.infra_components;

import com.hackeruso.automation.selenium.DriverFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public interface IModelParser<R> {

    R toRow(WebElement rowElm);

    default WebElement getColumnElmIfVisible(WebElement parentElm, By childBy) {
        boolean visible = DriverFactory.isChildElmVisible(parentElm, childBy);
        return visible ? parentElm.findElement(childBy) : null;
    }

    default String getText(WebElement parentElm, By childBy) {
        WebElement columnElm = this.getColumnElmIfVisible(parentElm, childBy);
        return columnElm == null ? "NONE" : columnElm.getText();
    }

    default String getAttribute(WebElement parentElm, By childBy, String attributeName) {
        WebElement columnElm = this.getColumnElmIfVisible(parentElm, childBy);
        return columnElm == null ? "NONE" : columnElm.getAttribute(attributeName);
    }
}
