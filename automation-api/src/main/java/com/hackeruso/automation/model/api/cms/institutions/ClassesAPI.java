package com.hackeruso.automation.model.api.cms.institutions;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.pages.cms.institutions.classes.ClassType;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsClassesApi;
import io.swagger.client.model.*;

import java.util.Collections;
import java.util.List;

public class ClassesAPI extends BaseAPI {
    private final CmsClassesApi cmsClassesApi = new CmsClassesApi();
    private final InstitutionsAPI institutionsAPI;
    private final CollegesAPI collegesAPI;

    public ClassesAPI(String host, String token) {
        super(host, token);
        cmsClassesApi.setApiClient(restApi.getClient());
        institutionsAPI = new InstitutionsAPI(host, token);
        collegesAPI = new CollegesAPI(host, token);
    }

    public void createNewClass(String institutionName, String collegeName, String className, ClassType type, String startDate, String endDate) throws ApiException {
        NewClassPayload newClassPayload = new NewClassPayload();
        int institutionId = institutionsAPI.getInstitutionIdByName(institutionName);
        int collegeId = collegesAPI.getCollegeIdByName(collegeName);
        newClassPayload.setInstitution(institutionId);
        newClassPayload.setCollegeId(collegeId);
        newClassPayload.setName(className);
        newClassPayload.setStatus(true);
        newClassPayload.setLuCourseType(1);
        newClassPayload.setGracePeriod(null);
        newClassPayload.setEndAt(endDate);//"2021-04-26"
        newClassPayload.setStartAt(startDate);//"2021-04-27"
        newClassPayload.setType(type.typeId);
        InstitutionTypeCreatedResponse res = cmsClassesApi.cmsCreateClass(token, newClassPayload);
        System.out.println(res.getMsg());
    }

    public List<DataClass> getClassesList(int resultsPerPage, int page, int status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        GetTableClassResponse getTable = cmsClassesApi.getClasses(token, resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
        writeRequestParamsToLog(getTable);
        return getTable.getData();
    }

    public int getClassIdByName(String className) throws ApiException {
        List<DataClass> classLst = getClassesList(10, 1, 1, "", true, className, className);
        return classLst.get(0).getId();
    }

    public void deleteClass(String className) throws ApiException {
        DeleteClassPayLoad deleteBody = new DeleteClassPayLoad();
        int id = getClassIdByName(className);
        deleteBody.setData(Collections.singletonList(id));
        deleteBody.setMode("Delete");
        writeRequestParamsToLog(deleteBody);
        cmsClassesApi.cmsDeleteClass(token, deleteBody);
    }
}
