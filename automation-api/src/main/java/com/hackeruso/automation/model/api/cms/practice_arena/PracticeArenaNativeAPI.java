package com.hackeruso.automation.model.api.cms.practice_arena;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hackeruso.automation.model.api.ServiceApiCommon;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PracticeArenaNativeAPI extends ServiceApiCommon<JsonElement> {

    public PracticeArenaNativeAPI(String host, String token) {
        super(host, token);
    }

    public JsonArray getModulesDetailedInfo() throws IOException {
        String path = "/api/syllabi?type=modules_pav2";
        return get(path).getAsJsonArray();
    }

    public JsonObject getLessonInfoByModuleAndLessonIds(String moduleId, String lessonId) throws IOException {
        if (moduleId == null || lessonId == null) {
            throw new RuntimeException("The moduleId or lessonId is null");
        }

        JsonArray modulesInfo = getModulesDetailedInfo();

        for (JsonElement moduleElement : modulesInfo) {
            if (moduleId.equals(moduleElement.getAsJsonObject().get("id").getAsString())) {
                JsonArray lessonsInfo = moduleElement.getAsJsonObject().get("pav2_lessons").getAsJsonArray();

                for (JsonElement lessonElement : lessonsInfo) {
                    if (lessonId.equals(lessonElement.getAsJsonObject().get("lesson_id").getAsString())) {
                        String path = String.format("/api/pav2/lesson?lesson=%s&module=%s", lessonId, moduleId);
                        return get(path).getAsJsonObject();
                    }
                }
            }
        }

        return null;
    }

    public Map<Integer, String[]> getSolutionForWordGameLab(String moduleId, String lessonId, String labId) throws IOException {
        JsonObject lessonObject = getLessonInfoByModuleAndLessonIds(moduleId, lessonId);
        JsonArray topicsJsonArray = lessonObject.get("topics").getAsJsonArray();

        for (JsonElement topic : topicsJsonArray) {
            JsonObject topicJsonObject = getJsonObject(topic);
            JsonObject topicAsset = getJsonObject(getJsonArray(topicJsonObject.get("topic_assets")).get(0));
            JsonObject metaData = getJsonObject(topicAsset.get("meta_data"));

            if (getString(metaData, "id").equalsIgnoreCase(labId)) {
                JsonArray tasksAsJsonArray = getJsonArray(metaData.get("tasks"));
                return fillSolutionForWordGameLabToMap(tasksAsJsonArray);
            }
        }
        return new HashMap<>();
    }

    private Map<Integer, String[]> fillSolutionForWordGameLabToMap(JsonArray tasksAsJsonArray) {
        Map<Integer, String[]> map = new LinkedHashMap<>();

        for (JsonElement task : tasksAsJsonArray) {
            JsonObject taskAsJsonObject = getJsonObject(task);
            Integer taskOrder = Integer.valueOf(getString(taskAsJsonObject, "order"));
            String[] taskSolution = getString(taskAsJsonObject, "solution").split("\\n\\n");

            map.put(taskOrder, taskSolution);
        }

        return map;
    }

    private JsonArray getJsonArray(JsonElement jsonElement) {
        return jsonElement.getAsJsonArray();
    }

    private JsonObject getJsonObject(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject();
    }

    private String getString(JsonObject jsonObject, String key) {
        return jsonObject.get(key).getAsString();
    }

    @Override
    protected JsonElement deserializeResponse(String bodyStr) {
        Gson g = new Gson();
        return g.fromJson(bodyStr, JsonElement.class);
    }
}
