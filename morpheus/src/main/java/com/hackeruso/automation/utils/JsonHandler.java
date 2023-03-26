package com.hackeruso.automation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

public class JsonHandler {
    private static final Gson GSON = new GsonBuilder().create();
    private JsonHandler() { }

    public static JsonObject asListObject(String json, String path) {
        String[] paths = path.split("\\.");
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        for (int i = 0; i < paths.length - 1; i++) {
            jsonObject = jsonObject.getAsJsonObject(paths[i]);
        }
        return jsonObject.getAsJsonObject(paths[paths.length - 1]);
    }
    public static JsonArray asList(String json, String path) {
        String[] paths = path.split("\\.");
        JsonObject jsonObject = GSON.fromJson(json, JsonObject.class);
        for (int i = 0; i < paths.length - 1; i++) {
            jsonObject = jsonObject.getAsJsonObject(paths[i]);
        }
        return jsonObject.getAsJsonArray(paths[paths.length - 1]);
    }

    public static <T> List<T> getJsonAsClassObjectList(String toParse , Class<T[]> clazz){
        T[] list = GSON.fromJson(toParse, clazz);
        return Arrays.asList(list);
    }

    public static <T> T getJsonAsClassObject(String toParse, Class<T> clazz){
        return GSON.fromJson(toParse, clazz);
    }
}
