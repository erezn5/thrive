package com.hackeruso.automation.model.api.aws;

import com.hackeruso.automation.conf.EnvConf;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.List;

public class AwsCognitoClient {

    private final CognitoIdentityProviderClient COGNITO_CLIENT;
    private final String USER_POOL_ID = EnvConf.getProperty("aws.user.pool.id");

    public AwsCognitoClient (){
        COGNITO_CLIENT = CognitoIdentityProviderClient.builder()
               .region(Region.US_EAST_1)
               .build();
    }

    public ListUsersResponse listUsers(String filter) {
        ListUsersResponse response = null;
        try {
            // List only users with specific email.
//            String filter = "email ^= \"automation_parallel_\"";
            ListUsersRequest usersRequest = ListUsersRequest.builder()
                    .userPoolId(USER_POOL_ID)
                    .filter(filter)
                    .build();

            response = COGNITO_CLIENT.listUsers(usersRequest);
        } catch (CognitoIdentityProviderException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return response;
    }

    public void deleteAllCognitoUsers(){
        String filter = "email ^= \"automation_parallel_\"";
        ListUsersResponse response = listUsers(filter);
        AdminDeleteUserResponse adminDeleteUserResponse;
        AdminDeleteUserRequest adminDeleteUserRequest;

        List<UserType> cognitoUserLst = response.users();
        for(UserType user: cognitoUserLst){
            System.out.println("User with filter applied " + user.username() + " Status " + user.userStatus());
        }
        for (UserType userType : cognitoUserLst) {
            adminDeleteUserRequest = AdminDeleteUserRequest.builder().userPoolId(USER_POOL_ID)
                    .username(userType.username()).build();
            adminDeleteUserResponse = COGNITO_CLIENT.adminDeleteUser(adminDeleteUserRequest);
            userType.attributes().forEach(userItem -> {
                System.out.println("Value: " + userItem.value() + " name: " + userItem.name());
            });
        }
    }
}
