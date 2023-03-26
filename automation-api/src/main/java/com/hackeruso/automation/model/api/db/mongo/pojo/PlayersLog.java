package com.hackeruso.automation.model.api.db.mongo.pojo;

import com.google.gson.annotations.SerializedName;

public class PlayersLog {
    @SerializedName("_id")
    private PodInstanceDocument.ID id;
    @SerializedName("playTime")
    private int playTime;
    @SerializedName("startTime")
    private int startTime;
    @SerializedName("stopTime")
    private int stopTime;

    public PodInstanceDocument.ID getId() {
        return id;
    }

    public int getPlayTime() {
        return playTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getStopTime() {
        return stopTime;
    }
}
