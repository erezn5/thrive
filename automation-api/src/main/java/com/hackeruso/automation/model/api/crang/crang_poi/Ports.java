package com.hackeruso.automation.model.api.crang.crang_poi;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Ports {
    @SerializedName("displayType")
    String displayType;
    @SerializedName("endPoint")
    String endPoint;
    @SerializedName("originalPort")
    int originalPort;
    @SerializedName("port")
    int port;
    @SerializedName("expectedHttpCode")
    int expectedHttpCode;
    @SerializedName("iframeFeatures")
    List<String> iframeFeatures;
    @SerializedName("protocolType")
    String protocolType;

    public String getDisplayType() {
        return displayType;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public int getOriginalPort() {
        return originalPort;
    }

    public int getPort() {
        return port;
    }

    public int getExpectedHttpCode() {
        return expectedHttpCode;
    }

    public List<String> getIframeFeatures() {
        return iframeFeatures;
    }

    public String getProtocolType() {
        return protocolType;
    }

    @Override
    public String toString() {
        return "Ports{" +
                "displayType='" + displayType + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", originalPort=" + originalPort +
                ", port=" + port +
                ", expectedHttpCode=" + expectedHttpCode +
                ", iframeFeatures=" + iframeFeatures +
                ", protocolType='" + protocolType + '\'' +
                '}';
    }
}
