package com.hackeruso.automation.model.api.crang.crang_poi;

import com.google.gson.annotations.SerializedName;

public class CheckerResponse {
    @SerializedName("status")
    String status;
    @SerializedName("podInstanceUserToken")
    String userToken;
    @SerializedName("version")
    String version;
    @SerializedName("data")
    CheckerData data;

    public String getUserToken() {
        return userToken;
    }

    public String getVersion() {
        return version;
    }

    public CheckerData getData() {
        return data;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CheckerResponse{" +
                "status='" + status + '\'' +
                ", userToken='" + userToken + '\'' +
                ", version='" + version + '\'' +
                ", data=" + data +
                '}';
    }
}
