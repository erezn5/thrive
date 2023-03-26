package com.hackeruso.automation.model.api;

import io.swagger.client.ApiException;
import io.swagger.client.api.VersionsApi;
import io.swagger.client.model.CywappResponse;

public class VersionAPI extends BaseAPI {

    private final VersionsApi versionsApi = new VersionsApi();
    public VersionAPI(String host, String token) {
        super(host, token);
        versionsApi.setApiClient(restApi.getClient());
    }

    public void getCywappVersion() throws ApiException {
        CywappResponse ver = versionsApi.getCywappVersion();
        String version = ver.getVersion();
        String crangVersion = versionsApi.getCrangVersion();
    }
}
