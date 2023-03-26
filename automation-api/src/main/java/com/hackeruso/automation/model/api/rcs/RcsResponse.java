package com.hackeruso.automation.model.api.rcs;

import com.google.gson.annotations.SerializedName;

public class RcsResponse {
    @SerializedName("status")
    public String status;
    @SerializedName("data")
    public RcsData rcsData;
}

