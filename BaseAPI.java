package com.hackeruso.automation.model.api;

import com.hackeruso.automation.conf.EnvConf;
import io.swagger.client.ApiClient;

public class BaseAPI extends BaseAPIClient<ApiClient> {

    protected String token;
    protected static final int NOT_FOUND = EnvConf.getAsInteger("not_found.entity");

    public BaseAPI(String host, String token) {
        super(host, new ClientApiImpl(new ApiClient()));
        this.token = token;
        initClient(host);
    }

    @Override
    protected void initClient(String host) {
        if (token == null) {
            throw new IllegalArgumentException("'Authorization token' param must be initialize");
        } else {
            restApi.addDefaultHeader("Authorization", token);
        }
    }
}
