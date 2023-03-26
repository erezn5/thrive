package com.hackeruso.automation.model.api.cms;

public enum CmsPageStatus {
    ACTIVE(1),
    INACTIVE(0);

    CmsPageStatus(int index){
        this.index = index;
    }

    public final int index;

    public int getIndex() {
        return index;
    }
}
