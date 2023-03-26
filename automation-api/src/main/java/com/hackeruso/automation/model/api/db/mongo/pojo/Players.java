package com.hackeruso.automation.model.api.db.mongo.pojo;

import com.google.gson.annotations.SerializedName;

public class Players {
    @SerializedName("playTime")
    private int playTime;
    @SerializedName("startTime")
    private StartTime startTime;
    @SerializedName("_id")
    private PodInstanceDocument.ID id;

    private static class StartTime {
        @SerializedName("$date")
        private long date;
    }

    public int getPlayTime() {
        return playTime;
    }

    public StartTime getStartTime() {
        return startTime;
    }

    public PodInstanceDocument.ID getId() {
        return id;
    }
}
