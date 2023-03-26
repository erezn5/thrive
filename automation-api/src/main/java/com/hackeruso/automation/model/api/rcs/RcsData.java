package com.hackeruso.automation.model.api.rcs;

import com.google.gson.annotations.SerializedName;

public class RcsData {
    @SerializedName("access_token")
    public String challengeToken;
    @SerializedName("url")
    public String url;
}
