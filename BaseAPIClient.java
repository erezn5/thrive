package com.hackeruso.automation.model.api;

import org.awaitility.Duration;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public abstract class BaseAPIClient<Client> {

    protected final RestClientApi<Client> restApi;
    private final String host;
    protected abstract void initClient(String host);

    public BaseAPIClient(String host, RestClientApi<Client> restApi){
        this.host = host;
        this.restApi = restApi;
        init();
    }

    private void init(){
        restApi.setVerifyingSsl(true);
        String basePath = restApi.getBasePath();
        basePath = basePath.replace(basePath, host + "/api");
        //todo here if the swagger will contain other host we can replace it here [basePath = basePath.replace("localhost", host)
        restApi.setBasePath(basePath);
    }

    protected void writeRequestParamsToLog(Object request){
        String logMsg = String.format("request params=[%s]", request);
        Log.info(logMsg);
    }

    protected void printResponse(Object response){
        String logMsg = String.format("response params=[%s]", response);
        Log.info(logMsg);
    }

    protected void printBody(Object response){
        String logMsg = String.format("request params=[%s]", response);
        Log.info(logMsg);
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.getTimeUnit().toMillis(duration.getValue()));
        } catch (InterruptedException e) {
            Log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected static String randSuffix(String prefix){
        return prefix + "_" + System.nanoTime();
    }
}
