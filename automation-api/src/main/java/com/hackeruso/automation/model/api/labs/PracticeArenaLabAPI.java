package com.hackeruso.automation.model.api.labs;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hackeruso.automation.model.api.ServiceApiCommon;
import com.hackeruso.automation.model.api.cms.practice_arena.PracticeArenaAPI;
import com.hackeruso.automation.model.api.cms.practice_arena.PracticeArenaNativeAPI;
import com.hackeruso.automation.utils.DateHandler;
import com.hackeruso.automation.utils.Waiter;
import io.swagger.client.ApiException;
import org.awaitility.Duration;
import org.awaitility.core.Condition;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class PracticeArenaLabAPI extends ServiceApiCommon<JsonObject> {
    private final PracticeArenaAPI practiceArenaAPI;
    private final PracticeArenaNativeAPI practiceArenaNativeAPI;

    protected String MODULE_ID, LESSON_ID, LAB_ID;

    public enum TaskType {
        NOT_TESTABLE, TESTABLE, FLAG;

        public static TaskType getType(String typeId) {
            return Arrays.stream(values()).filter(type -> type.ordinal() == Integer.parseInt(typeId)).findFirst().orElse(null);
        }
    }

    public PracticeArenaLabAPI(String host, String token) {
        super(host);
        addAcceptJsonHeader();
        addAuthorizationHeader(token);

        practiceArenaAPI = new PracticeArenaAPI(host, token);
        practiceArenaNativeAPI = new PracticeArenaNativeAPI(host, token);
    }

    private void setIdsByItsEntityName(String moduleName, String lessonName, String labName) throws ApiException {
        MODULE_ID = practiceArenaAPI.getModuleIdByModuleName(moduleName);
        LESSON_ID = practiceArenaAPI.getLessonIdByLessonName(lessonName);
        LAB_ID = practiceArenaAPI.getLabIdByName(labName);
    }

    public Map<Integer, String[]> getSolutionForWordGameLab(String moduleName, String lessonName, String labName) throws IOException, ApiException {
        setIdsByItsEntityName(moduleName, lessonName, labName);
        return practiceArenaNativeAPI.getSolutionForWordGameLab(MODULE_ID, LESSON_ID, LAB_ID);
    }

    public List<TaskParametersToTrigger> launchTheVirtualLabAndReturnTaskTriggerList(String moduleName, String lessonName, String labName) throws ApiException, IOException {
        launchTheVirtualLab(moduleName, lessonName, labName);
        return getDataToVerifyAllTasksInTheLab();
    }

    public Boolean launchTheVirtualLab(String moduleName, String lessonName, String labName) throws ApiException {
        setIdsByItsEntityName(moduleName, lessonName, labName);

        String path = String.format("/api/pav2/machine-info/%s?lesson=%s&module=%s", LAB_ID, LESSON_ID, MODULE_ID);
        Log.i("Trigger LAB [%s] with path: %s ", DateHandler.convertLongDateFormatByPattern("HH:mm:ss,SSS", new Date().getTime()), path);

        Condition<Boolean> condition = () -> {
            try {
                JsonObject jsonObject = get(path);
                String status = jsonObject.get("status").getAsString();
                Log.i("Response: status=[%s]", status);
                if(status.equalsIgnoreCase("Ready")) {
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                throw new RuntimeException("Cannot Trigger the Lab in 90 seconds. " + e.getMessage());
            }
        };
        return Waiter.waitCondition(new Duration(90, TimeUnit.SECONDS), condition, Duration.FIVE_SECONDS);
    }

    @Override
    protected JsonObject deserializeResponse(String bodyStr) {
        Gson g = new Gson();
        return g.fromJson(bodyStr, JsonObject.class);
    }

    private JsonObject getJsonObject(JsonObject jsonObject, String key) {
        return jsonObject.get(key).getAsJsonObject();
    }

    private JsonArray getJsonArray(JsonObject jsonObject, String key) {
        return jsonObject.get(key).getAsJsonArray();
    }

    private String getString(JsonObject jsonObject, String key) {
        return jsonObject.get(key).getAsString();
    }

    public String solveTheTaskAndGetItStatusCode(TaskType type, String topicId, String labId, String taskId, String flagSolution) throws IOException {
        JsonObject responseJsonObject;
        String path, body;
        String statusCode;

        switch (type) {
            case NOT_TESTABLE:
                path = String.format("/api/pav2/test-untestable-task/%s/%s/%s", topicId, labId, taskId);
                responseJsonObject = get(path);
                statusCode = getStatusCode(getJsonObject(responseJsonObject, "answer"));
                Log.i("\nTrigger TASK of type=0 \n\tURL=[%s]\n\tstatus_code=[%s]", path, statusCode);
                return statusCode;
            case TESTABLE:
                path = String.format("/api/pav2/test-task/%s/%s/%s", topicId, labId, taskId);
                responseJsonObject = get(path);
                statusCode = getStatusCode(responseJsonObject);
                Log.i("\nTrigger TASK of type=1 \n\tURL=[%s]\n\tstatus_code=[%s]", path, statusCode);
                return statusCode;
            case FLAG:
                path = "/api/pav2/test/flag";
                body = String.format("{\"topic_id\":\"%s\",\"lab_uuid\":\"%s\",\"task_id\":\"%s\",\"input\":\"%s\"}", topicId, labId, taskId, flagSolution);
                responseJsonObject = postJson(path, body);

                try {
                    statusCode = getStatusCode(getJsonObject(responseJsonObject, "data"));
                } catch (Exception e) {
                    Log.i("!!! Missing Solution in TASK, json=[{%s}]", body);
                    statusCode = null;
                }

                Log.i("\nTrigger TASK of type=2 \n\tURL=[%s]\n\tBody=[%s]\n\tstatus_code=[%s]", path, body, statusCode);
                return statusCode;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private String getStatusCode(JsonObject jsonObject) {
        return getString(jsonObject, "status_code");
    }

    private List<TaskParametersToTrigger> getDataToVerifyAllTasksInTheLab() throws IOException {
        List<TaskParametersToTrigger> collectTriggerData = new ArrayList<>();

        JsonObject lessonObject = practiceArenaNativeAPI.getLessonInfoByModuleAndLessonIds(MODULE_ID, LESSON_ID);
        JsonArray topicsAsJsonArray = getJsonArray(lessonObject, "topics");

        for (JsonElement topic : topicsAsJsonArray) {
            JsonObject topicAsJsonObject = topic.getAsJsonObject();
            String topicId = getString(topicAsJsonObject, "id");
            JsonArray topicAssetsAsJsonArray = getJsonArray(topicAsJsonObject, "topic_assets");

            for (JsonElement topicAsset : topicAssetsAsJsonArray) {
                JsonObject topicAssetAsJsonObject = topicAsset.getAsJsonObject();
                String labId = getString(topicAssetAsJsonObject, "content_id");

                if (!labId.equalsIgnoreCase(LAB_ID)) {
                    continue;
                }

                JsonObject metaDataAsJsonObject = getJsonObject(topicAssetAsJsonObject, "meta_data");
                String labName = getString(metaDataAsJsonObject, "name");
                Log.i("Lab=[%s]", labName);

                JsonArray tasksAsJsonArray = getJsonArray(metaDataAsJsonObject, "tasks");
                for (JsonElement task : tasksAsJsonArray) {
                    JsonObject taskAsJsonObject = task.getAsJsonObject();
                    String taskId = getString(taskAsJsonObject, "id");
                    TaskType type = TaskType.getType(getString(taskAsJsonObject, "type"));
                    String flagSolution = type.equals(TaskType.FLAG) ? getString(getJsonObject(taskAsJsonObject, "flag"), "solution") : "";

                    collectTriggerData.add(new TaskParametersToTrigger(type, topicId, labId, taskId, flagSolution));
                }
            }
        }

        return collectTriggerData;
    }

    public static class TaskParametersToTrigger {
        public TaskType type;
        public String topicId;
        public String labId;
        public String taskId;
        public String flagSolution;

        public TaskParametersToTrigger(TaskType type, String topicId, String labId, String taskId, String flagSolution) {
            this.type = type;
            this.topicId = topicId;
            this.taskId = taskId;
            this.labId = labId;
            this.flagSolution = flagSolution;
        }
    }
}
