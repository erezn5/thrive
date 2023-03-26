package com.hackeruso.automation.model.api;
import io.swagger.client.ApiClient;

public class ClientApiImpl implements RestClientApi<ApiClient> {

    private final ApiClient client;

    public ClientApiImpl(ApiClient client) {
        this.client = client;
        this.client.setConnectTimeout(CONNECTION_TIMEOUT_MILLI);
        this.client.setReadTimeout(RESPONSE_TIMEOUT_MILLI);
    }

    @Override
    public RestClientApi<ApiClient> setVerifyingSsl(boolean verifyingSsl) {
        client.setVerifyingSsl(verifyingSsl);
        return this;
    }

    @Override
    public RestClientApi<ApiClient> setBasePath(String basePath) {
        client.setBasePath(basePath);
        return this;
    }

    @Override
    public String getBasePath() {
        return client.getBasePath();
    }

    @Override
    public RestClientApi<ApiClient> addDefaultHeader(String key, String value) {
        client.addDefaultHeader(key,value);
        return this;
    }

    @Override
    public ApiClient getClient() {
        return client;
    }
}
