package com.hackeruso.automation.model.api.cms.users;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.cms.institutions.ClassesAPI;
import com.hackeruso.automation.model.api.cms.institutions.CollegesAPI;
import com.hackeruso.automation.model.api.cms.institutions.InstitutionsAPI;
import com.hackeruso.automation.model.api.db.SQL;
import com.hackeruso.automation.model.pages.cms.users.UserRole;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsUsersApi;
import io.swagger.client.model.*;

import java.util.ArrayList;
import java.util.List;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class UsersAPI extends BaseAPI {

    private final CmsUsersApi usersApi = new CmsUsersApi();
    private final CollegesAPI collegesAPI;
    private final InstitutionsAPI institutionsAPI;
    private final ClassesAPI classesAPI;

    public UsersAPI(String host, String token) {
        super(host, token);
        usersApi.setApiClient(restApi.getClient());
        collegesAPI = new CollegesAPI(host, token);
        institutionsAPI = new InstitutionsAPI(host, token);
        classesAPI = new ClassesAPI(host, token);
    }

    public void updateInternalUserByUserEmail(String userMail, boolean internalStatus) throws ApiException {
        String userName = getUserNameByUserMail(userMail);
        int userId = getUserIdByName(userName, 1, 250);
        InternalUserBodyReq internalUserBodyReq = new InternalUserBodyReq();

        internalUserBodyReq.setUserId(userId);
        internalUserBodyReq.setDelete(internalStatus);
        printBody(internalUserBodyReq);
        String response = usersApi.setInternalUser(token, internalUserBodyReq);
        printResponse(response);
        System.out.println(response);
    }

    /**
     *
     * @param name - name of the user to be created
     * @param email - email of the user
     * @param phone - phone of the user
     * @param password - password of the user
     * @return "User Created Successfully"
     * userRole - 33 - student, 21 - demo, 58 - teacher, 67 - master, 149 - candidate (use UserRole enum class)
     * @throws ApiException
     */

    public String createAdminUser(String name, String email, String phone, String password) throws ApiException {
        NewUserPayload body = setUserBody(name, UserRole.ADMIN, email, phone, password, -1);
        return usersApi.createUser(token, body);
    }

    private NewUserPayload setUserBody(String name, UserRole role, String email, String phone, String password, int institutionId, boolean... status) {
        NewUserPayload body = new NewUserPayload();
        if(status.length!=0){
            body.setStatus(status[0]);
        }else {
            body.setStatus(true);
        }
        body.setRole(role.apiRoleCode);
        body.setName(name);
        body.setEmail(email);
//        body.setPhone(phone);
        body.setPassword(password);
        body.setPasswordConfirmation(password);
        body.setCountryId(231);
        body.setDialCode("+1");
        if(institutionId!=-1) {
            body.setInstitution(institutionId);
        }
        printBody(body);
        return body;
    }

    public String createUser(String name, UserRole userRole, String email, String phone, String password, String institutionName, String collegeName, String className, boolean... status) throws ApiException {
        int institutionId = institutionsAPI.getInstitutionIdByName(institutionName);
        NewUserPayload body = setUserBody(name, userRole, email, phone, password, institutionId, status);

        CollegeItemData collegeItemData = new CollegeItemData();
        collegeItemData.setName(collegeName);
        collegeItemData.setInstitutionId(institutionId);
        collegeItemData.setId(collegesAPI.getCollegeIdByName(collegeName));

        ClassItemData classItemData = new ClassItemData();
        classItemData.setName(className);
        classItemData.setCollegeId(collegesAPI.getCollegeIdByName(collegeName));
        classItemData.setId(classesAPI.getClassIdByName(className));

        UserCollegesRequest userCollegesRequest = new UserCollegesRequest();
        userCollegesRequest.add(collegeItemData);

        UserClassesRequest userClassesRequest = new UserClassesRequest();
        userClassesRequest.add(classItemData);

        body.setColleges(userCollegesRequest);
        body.setClasses(userClassesRequest);

        printBody(body);

        String response = usersApi.createUser(token, body);
        printResponse(response);

        try {
            SQL sql = new SQL();
            sql.setNotFirstLogin(email);
            sql.closeDBConnection();
        } catch (Exception sqlException) {
            Log.error("Error is: [" + sqlException.getMessage() + "]");
        }

        return response;
    }

    public List<DataUsers> getAllUsers() throws ApiException {
        return getAllUsers(1, 250, "", 1, "name");
    }

    public List<DataUsers> getAllUsers(int pageNumber, int resultsPerPage, String searchValue, int page, String searchBy) throws ApiException {
        GetTableResponseUser getUsers =
                usersApi.getUsers(
                        token, pageNumber, resultsPerPage, page,
                        "", "", true, searchValue, searchBy);
        printBody(getUsers.getData());
        return getUsers.getData();
    }

    public String deleteUsersByName(List<String> userNameList) throws ApiException {
        DeleteUserBody deleteUserBody = new DeleteUserBody();
        deleteUserBody.setMode("Delete");
        List<Integer> userIdList = new ArrayList<>();
        for(String userName: userNameList){
            Integer id = getUserIdByName(userName, 1, 50);
            userIdList.add(id);
        }
        deleteUserBody.setData(userIdList);
        printBody(deleteUserBody);
        return usersApi.deleteUsers(token, deleteUserBody);
    }

    public String getUserNameByUserMail(String userMail) throws ApiException {
        List<DataUsers> userList = getAllUsers(1, 250, userMail, 1, "email");
        DataUsers user = userList.stream().filter(userData->userData.getEmail().contains(userMail)).findFirst().orElse(null);
        assert user != null;
        Log.i("User found=[%s], with user id=[%s]", user, user.getName());
        return user.getName();
    }

    public Integer getUserIdByName(String name, int pageNumber, int resultsPerPage) throws ApiException {
        List<DataUsers> userList = getAllUsers(pageNumber, resultsPerPage, name, 1, "name");
        DataUsers user = userList.stream().filter(userData->userData.getName().contains(name)).findFirst().orElse(null);
        assert user != null;
        Log.i("User found=[%s], with user id=[%d]", user, user.getId());
        return user.getId();
    }

}
