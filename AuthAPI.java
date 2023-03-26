package com.hackeruso.automation.model.api;

import com.hackeruso.automation.conf.EnvConf;
import io.swagger.client.ApiException;
import io.swagger.client.api.LoginApi;
import io.swagger.client.model.LoginBody;

public class AuthAPI extends BaseAPI {
    private final String ADMIN_PASSWORD = EnvConf.getProperty("hackeruso.admin.user.password");
    private final LoginApi loginApi = new LoginApi();

    public AuthAPI(String host) {
        super(host, "");
        loginApi.setApiClient(restApi.getClient());
    }

    public String getToken(String username) throws ApiException {
        return generateTokenWithCredentials(username, ADMIN_PASSWORD);
    }

    public String generateToken(String adminUserName) throws ApiException {
        if (token != null) {
            token = null;
        }
        token = getToken(adminUserName);
        return String.format("Bearer %s", token);
    }

    protected String generateTokenWithCredentials(String username, String password) throws ApiException {
        LoginBody body = new LoginBody();
        body.setEmail(username);
        body.setPassword(password);
        body.setCaptcha("");
        body.setCaptchaShow(false);
        body.setRemember(false);
        printBody(body);
        return loginApi.loginUser(body).getData().getToken();
    }
}
