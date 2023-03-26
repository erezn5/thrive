package com.hackeruso.automation.model.api.crang;

import com.hackeruso.automation.model.api.ServiceApiCommon;
import com.hackeruso.automation.model.api.crang.crang_poi.CheckerResponse;

import java.io.IOException;

public class CheckerAPI extends ServiceApiCommon<CheckerResponse> {
    protected CheckerAPI(String host, String token) {
        super(host, token);
    }

    public CheckerResponse sendCheckerPostRequest(String path, String body) throws IOException {
        return postJson(path, body);
    }

    @Override
    protected CheckerResponse deserializeResponse(String bodyStr) {
        return GSON.fromJson(bodyStr , CheckerResponse.class);
    }
}
