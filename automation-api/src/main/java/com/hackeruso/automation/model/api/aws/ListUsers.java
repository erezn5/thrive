//package com.hackeruso.automation.model.api.aws;
//
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
//import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
//
//public class ListUsers {
//
//    public static void main(String[] args) {
//    final String USAGE = "\n" +
//            "Usage:\n" +
//            "    <userPoolId> \n\n" +
//            "Where:\n" +
//            "    userPoolId - the ID given to your user pool when it's created.\n\n" ;
//
////    if (args.length != 1) {
////        System.out.println(USAGE);
////        System.exit(1);
////    }
//    String userPoolId = "us-east-1_qvWp8iA5f";
//    CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
//            .region(Region.US_EAST_1)
//            .build();
//
//    listAllUsers(cognitoClient, userPoolId );
//    listUsersFilter(cognitoClient, userPoolId );
////    listAllUsersPools(cognitoClient);
//    cognitoClient.close();
//}
//
//    private static void listAllUsersPools(CognitoIdentityProviderClient cognitoClient) {
//        try {
//            ListUserPoolsRequest request = ListUserPoolsRequest.builder()
//                    .maxResults(10)
//                    .build();
//
//            ListUserPoolsResponse response = cognitoClient.listUserPools(request);
//            response.userPools().forEach(userpool -> {
//                        System.out.println("User pool " + userpool.name() + ", User ID " + userpool.id() );
//                    }
//            );
//
//        } catch (CognitoIdentityProviderException e){
//            System.err.println(e.awsErrorDetails().errorMessage());
//            System.exit(1);
//        }
//    }
//
//    // Shows how to list all users in the given user pool.
//    public static void listAllUsers(CognitoIdentityProviderClient cognitoClient, String userPoolId ) {
//
//        try {
//            ListUserPoolsRequest request = ListUserPoolsRequest.builder()
//                    .maxResults(10)
//                    .build();
//            // List all users
//            ListUsersRequest usersRequest = ListUsersRequest.builder()
//                    .userPoolId(userPoolId)
//                    .build();
//
//            ListUsersResponse response = cognitoClient.listUsers(usersRequest);
//            response.users().forEach(user -> {
//                        System.out.println("User " + user.username() + " Status " + user.userStatus() + " Created " + user.userCreateDate()  );
//                    }
//            );
//
//        } catch (CognitoIdentityProviderException e){
//            System.err.println(e.awsErrorDetails().errorMessage());
//            System.exit(1);
//        }
//    }
//
//    // Shows how to list users by using a filter.
//    public static void listUsersFilter(CognitoIdentityProviderClient cognitoClient, String userPoolId ) {
//
//        try {
//            // List only users with specific email.
//            String filter = "email ^= \"automation_parallel_\"";
//
//            ListUsersRequest usersRequest = ListUsersRequest.builder()
//                    .userPoolId(userPoolId)
//                    .filter(filter)
//                    .build();
//
//            ListUsersResponse response = cognitoClient.listUsers(usersRequest);
//
//            response.users().forEach(user -> {
//                        System.out.println("User with filter applied " + user.username() + " Status " + user.userStatus() + " Created " + user.userCreateDate() );
//                    }
//            );
//
//        } catch (CognitoIdentityProviderException e){
//            System.err.println(e.awsErrorDetails().errorMessage());
//            System.exit(1);
//        }
//    }
//}