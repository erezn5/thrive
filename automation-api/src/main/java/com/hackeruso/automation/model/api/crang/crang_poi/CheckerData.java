package com.hackeruso.automation.model.api.crang.crang_poi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CheckerData {
    @SerializedName("status_code")
    int statusCode;
    @SerializedName("title")
    String title;
    @SerializedName("text")
    String text;
    @SerializedName("accessPoint")
    String accessPoint;
    @SerializedName("ports")
    List<Ports> portsList;

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public List<Ports> getPortsList() {
        return portsList;
    }

    @Override
    public String toString() {
        return "CheckerData{" +
                "statusCode=" + statusCode +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", accessPoint='" + accessPoint + '\'' +
                ", portsList=" + portsList +
                '}';
    }
}
