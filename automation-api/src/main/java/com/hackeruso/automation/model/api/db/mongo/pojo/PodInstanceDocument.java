package com.hackeruso.automation.model.api.db.mongo.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PodInstanceDocument {
    @SerializedName("_id")
    private ID id;
    @SerializedName("status")
    private String status;
    @SerializedName("accessPoint")
    private String accessPoint;
    @SerializedName("podUuid")
    private String podUuid;
    @SerializedName("playTime")
    private String playTime;
    @SerializedName("maxTimeToJoin")
    private MaxTimeToJoin maxTimeToJoin;
    @SerializedName("exposedPorts")
    private List<ExposedPorts> exposedPortsList;
    @SerializedName("maxPlayers")
    private String maxPlayers;
    @SerializedName("players")
    private List<Players> playersList;
    @SerializedName("playersLog")
    private List<PlayersLog> playersLog;
    @SerializedName("deployType")
    private String deployType;
    @SerializedName("version")
    private String version;
    @SerializedName("createdAt")
    private CreatedAt createdAt;
    @SerializedName("updatedAt")
    private UpdatedAt updatedAt;
    @SerializedName("_v")
    private int v;
    @SerializedName("islandInstanceId")
    private String islandInstanceId;
    @SerializedName("loadTime")
    private int loadTime;

    public ID getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public String getPodUuid() {
        return podUuid;
    }

    public String getPlayTime() {
        return playTime;
    }

    public MaxTimeToJoin getMaxTimeToJoin() {
        return maxTimeToJoin;
    }

    public List<ExposedPorts> getExposedPortsList() {
        return exposedPortsList;
    }

    public String getMaxPlayers() {
        return maxPlayers;
    }

    public List<Players> getPlayersList() {
        return playersList;
    }

    public List<PlayersLog> getPlayersLog() {
        return playersLog;
    }

    public String getDeployType() {
        return deployType;
    }

    public String getVersion() {
        return version;
    }

    public CreatedAt getCreatedAt() {
        return createdAt;
    }

    public UpdatedAt getUpdatedAt() {
        return updatedAt;
    }

    public int getV() {
        return v;
    }

    public String getIslandInstanceId() {
        return islandInstanceId;
    }

    public int getLoadTime() {
        return loadTime;
    }

    public static class ID {
        @SerializedName("$oid")
        private String oid;
    }

    public static class MaxTimeToJoin{
        @SerializedName("$date")
        private long date;
    }

    public static class ExposedPorts{
        @SerializedName("originalPort")
        private int originalPort;
        @SerializedName("port")
        private int port;
        @SerializedName("displayType")
        private String displayType;
        @SerializedName("protocolType")
        private String protocolType;
        @SerializedName("endPoint")
        private String endPoint;
        @SerializedName("iframeFeatures")
        private List<String> iframeFeaturesList;
        @SerializedName("key")
        private int key;
        @SerializedName("_id")
        private ID id;


        private static class IframeFeatures {
            @SerializedName("GUACAMOLE_REFOCUS_IFRAME")
            private String guacamoleRefocusIframe;
        }
    }

    private static class CreatedAt {
        @SerializedName("$date")
        private long date;
    }

    private static class UpdatedAt {
        @SerializedName("$date")
        private long date;
    }
}
