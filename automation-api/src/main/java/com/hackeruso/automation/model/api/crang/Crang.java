package com.hackeruso.automation.model.api.crang;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.model.api.cms.practice_arena.PracticeArenaAPI;
import com.hackeruso.automation.model.api.crang.crang_poi.CheckerResponse;
import com.hackeruso.automation.ssh.Shell;
import io.swagger.client.ApiException;
import io.swagger.client.model.LabDataResponse;
import io.swagger.client.model.TaskItemData;

import java.io.IOException;
import java.util.List;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class Crang {
    private static final String SSH_USER = EnvConf.getProperty("crang.username");
    private static final String SSH_HOST = EnvConf.getProperty("crang.hostname");
    private static final String BASTION_PEM_FILE = EnvConf.getProperty("bastion.pem.file.location");
    private static final String LOCAL_HOST = "http://localhost";
    private static final String TOKEN = EnvConf.getProperty("crang.token");
    private static final String REMOTE_CRANG_HOST = EnvConf.getProperty("crang.remote.host");
    private static final CheckerAPI checkerAPI = new CheckerAPI(LOCAL_HOST, TOKEN);
    private static int FORWARDED_PORT;
    private final PracticeArenaAPI practiceArenaAPI;

    public Crang(PracticeArenaAPI practiceArenaAPI) throws Exception {
        establishCrangSSHConnection();
        this.practiceArenaAPI = practiceArenaAPI;
    }

    private void establishCrangSSHConnection() throws Exception {
        Shell.ChannelHandler channelHandler =
                Shell.builder()
                        .setBastionPemFile(BASTION_PEM_FILE)
                        .setHost(SSH_HOST).setUser(SSH_USER).setTimeout(300L).build();
        FORWARDED_PORT = channelHandler.portForwarding(8000, REMOTE_CRANG_HOST, 9008);
    }

    public CheckerResponse inspectLabReadiness(String podInstanceUserToken) throws IOException {
        String jsonBodyData = "{\n" +
                "    \"podInstanceUserToken\": \"" + podInstanceUserToken + "\"\n" +
                "}";
        return sendCommonDataRequest(jsonBodyData, ":%s/api/pod-instance/inspect");
    }

    public CheckerResponse startInstanceRequest(String labUUID) throws IOException {
        String jsonBodyData = "{\n" +
                "    \"podId\": \"" + labUUID + "\",\n" +
                "    \"playTime\": 4\n" +
                "}";
        //9da7c9aa-3ab6-4a76-8e5a-d7cf4e80ad6f
        return sendCommonDataRequest(jsonBodyData, ":%s/api/pod-instance/start");
    }

    public CheckerResponse sendLessonCheckRequest(String podInstanceUserToken, String taskUuid) throws IOException {
        String jsonBodyData = "{\n" +
                "    \"podInstanceUserToken\": \"" + podInstanceUserToken + "\",\n" +
                "    \"data\": {\n" +
                "        \"taskUuid\": \"" + taskUuid + "\"\n" +
                "    }\n" +
                "}";
        return sendCommonDataRequest(jsonBodyData, ":%s/api/pod-instance/lesson-check");
    }

    private CheckerResponse sendCommonDataRequest(String jsonBodyData, String s) throws IOException {
        String path = String.format(s, FORWARDED_PORT);
        return checkerAPI.sendCheckerPostRequest(path, jsonBodyData);
    }

    private enum LabStatus{
        READY("Ready"),
        PENDING("Pending"),
        ERROR("Error");

        private final String value;

        LabStatus(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }
    }

    public void startLabAndVerifyReadiness(String labName) throws IOException, ApiException, InterruptedException {
        LabDataResponse labDataResponse = practiceArenaAPI.getSpecificLabByName(labName);
        Log.i("Lab with the name=[%s] with the following id is=[%s]",labName, labDataResponse.getLab().getId());

        String podInstanceUserToken = startInstanceRequest(labDataResponse.getLab().getId()).getUserToken();
        String status = inspectLabReadiness(podInstanceUserToken).getStatus();

        while (!status.equals(LabStatus.READY.getValue())) {
            status = inspectLabReadiness(podInstanceUserToken).getStatus();
            Thread.sleep(5000);
        }

        List<TaskItemData> tasksLabLst = labDataResponse.getLab().getTasks();
        Log.i("Number of tasks are=[%d]", tasksLabLst.size());

        for (TaskItemData taskItemData : tasksLabLst) {
            Log.i("Task Id=[%s]", taskItemData.getId());

            CheckerResponse response = sendLessonCheckRequest(podInstanceUserToken, taskItemData.getId());
            Log.i("Response is=[%s]\n", response.toString());
        }
    }

    public void stopInstanceRequest(String podInstanceUserToken) throws IOException {
        String jsonBodyData = "{\n" +
                "    \"podInstanceUserToken\": \"" + podInstanceUserToken + "\",\n" +
                "}";
        sendCommonDataRequest(jsonBodyData, ":%s/api/pod-instance/stop");
    }
}
