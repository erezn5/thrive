package com.hackeruso.automation.model.api.rcs;

import com.hackeruso.automation.model.api.ServiceApiCommon;
import com.hackeruso.automation.model.api.db.SQL;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RCSApi extends ServiceApiCommon<RcsResponse>{

    private final SQL sql;

    public RCSApi(String host) throws Exception {
        super(host);
        sql = new SQL();
        addAuthorizationHeader(getRcsToken());
    }

    public RcsResponse sendCreateChallengeRequest() throws IOException {
        String body = "{\n" +
                "  \"challenge_id\": \"ed8800e9-a3d7-43b6-8728-87b342b075df\",\n" +
                "  \"redirect_url\": \"https://google.com\",\n" +
                "  \"duration\": 30,\n" +
                "  \"candidate\": {\n" +
                "    \"first_name\": \"Danni\",\n" +
                "    \"last_name\": \"David\",\n" +
                "    \"gender\": \"David\",\n" +
                "    \"occupation\": \"Israel\",\n" +
                "    \"country\": \"Israel\",\n" +
                "    \"city\": \"Haifa\",\n" +
                "    \"email\": \"test@gmail.com\",\n" +
                "    \"phone\": \"408-867-5309\"\n" +
                "  }\n" +
                "}";
        return postJson("/extsvc/rcs/create", body);
    }

    public String getRcsToken() throws SQLException {
        String GET_RCS_TOKEN_QUERY = "SELECT api_token FROM consumers;";
        ResultSet set = sql.executeQuery(GET_RCS_TOKEN_QUERY);
        String token = null;
        while(set.next()){
            token = set.getString("api_token");
        }
        sql.closeDBConnection();
        return "Bearer " + token;
    }

    @Override
    protected RcsResponse deserializeResponse(String bodyStr) {
        return GSON.fromJson(bodyStr, RcsResponse.class);
    }
}

