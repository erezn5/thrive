package com.hackeruso.automation.model.api;

import io.swagger.client.ApiClient;

public interface RestClientApi<Client> {

    int RESPONSE_TIMEOUT_MILLI = 60 * 1000;
    int CONNECTION_TIMEOUT_MILLI = 60 * 1000;

    RestClientApi<Client> setVerifyingSsl(boolean verifyingSsl);

    RestClientApi<Client> setBasePath(String basePath);

    String getBasePath();

    RestClientApi<Client> addDefaultHeader(String key, String value);

    ApiClient getClient();
}
