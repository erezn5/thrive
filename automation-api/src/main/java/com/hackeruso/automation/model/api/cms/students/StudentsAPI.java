package com.hackeruso.automation.model.api.cms.students;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.db.SQL;
import com.hackeruso.automation.utils.StringUtils;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsStudentsApi;
import io.swagger.client.model.DataUsers;
import io.swagger.client.model.GetTableResponseUser;

import java.util.List;

public class StudentsAPI extends BaseAPI {

    private final CmsStudentsApi cmsStudentsApi = new CmsStudentsApi();

    public enum StudentStatus {
        ACTIVE(1),
        INACTIVE(0);

        private final int code;

        StudentStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public StudentsAPI(String host, String token) {
        super(host, token);
        cmsStudentsApi.setApiClient(restApi.getClient());
    }

    public void setUserAsMaintenanceUser(String userName) throws Exception {
        SQL sql = new SQL();
        int studentId = getStudentIdByStudentName(userName);
        int id = Integer.parseInt(StringUtils.getRandomString(3, false, true));
        sql.executeUpdate(String.format("insert into maintenance_users (id, user_id) values (%d, %d);", id, studentId));
        sql.closeDBConnection();
    }

    private List<DataUsers> getAllStudentUsers(StudentStatus statusCode) throws ApiException {
        GetTableResponseUser getUsers = cmsStudentsApi.getStudents(token, statusCode.getCode(), 250, 1, "", "", true, "", "name");
        printBody(getUsers.getData());
        return getUsers.getData();
    }

    private int getStudentIdByStudentName(String studentName) throws ApiException {
        int studentId = findStudentIdByStatus(studentName, StudentStatus.ACTIVE);
        if (studentId == NOT_FOUND)
            studentId = findStudentIdByStatus(studentName, StudentStatus.INACTIVE);
        return studentId;
    }

    private int findStudentIdByStatus(String studentName, StudentStatus status) throws ApiException {
        List<DataUsers> allStudentUsers = getAllStudentUsers(status);
        return allStudentUsers.stream().filter(studentUser -> studentUser.getName().equalsIgnoreCase(studentName)).findFirst().map(DataUsers::getId).orElse(NOT_FOUND);
    }

    public void setStudentStatus(String studentName, boolean status) throws ApiException {
        int studentId = getStudentIdByStudentName(studentName);
        if (studentId != NOT_FOUND) {
            DataUsers dataUsers = new DataUsers();
            dataUsers.setId(studentId);
            dataUsers.setStatus(status ? 1 : 0);
            cmsStudentsApi.setStatus(token, dataUsers);
        }
    }
}
