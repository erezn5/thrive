package com.hackeruso.automation.model.api.cms.institutions;

import com.hackeruso.automation.model.api.BaseAPI;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsCollegeApi;
import io.swagger.client.model.*;

import java.util.List;
import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class CollegesAPI extends BaseAPI {

    CmsCollegeApi cmsCollegeApi = new CmsCollegeApi();
    InstitutionsAPI institutionsAPI;


    public CollegesAPI(String host, String token) {
        super(host, token);
        cmsCollegeApi.setApiClient(restApi.getClient());
        institutionsAPI = new InstitutionsAPI(host, token);
    }

    public List<Data> getCollegesList(int resultsPerPage, int page, int status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        GetTableResponse getTable = cmsCollegeApi.getColleges(token, resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
        writeRequestParamsToLog(getTable);
        return getTable.getData();
    }

    public void createNewCollege(String institutionName, String collegeName) throws ApiException {
        NewCollegePayload newCollegePayload = new NewCollegePayload();
        newCollegePayload.setName(collegeName);
        int institutionId = institutionsAPI.getInstitutionIdByName(institutionName);
        newCollegePayload.setInstitutionId(institutionId);
        newCollegePayload.setStatus(true);
        writeRequestParamsToLog(newCollegePayload);
        printBody(newCollegePayload);
        InstitutionTypeCreatedResponse institutionTypeCreatedResponse = cmsCollegeApi.cmsCreateCollege(token, newCollegePayload);
        printResponse(institutionTypeCreatedResponse);
        Log.info(institutionTypeCreatedResponse.getMsg());
    }

    public int getCollegeTypeId(String collegeName) throws ApiException {
        List<Data> collegeLst = getCollegesList(10, 1, 1, "", true, collegeName, collegeName);
        int collegeTypeId = collegeLst.get(0).getCollegeTypeId();
        Log.i("College type id is =[%s]", collegeTypeId);
        return collegeTypeId;
    }

    public int getCollegeIdByName(String collegeName) throws ApiException {
        List<Data> collegeLst = getCollegesList(10, 1, 1, "", true, collegeName, collegeName);
        int collegeId = collegeLst.get(0).getId();
        Log.i("College id is =[%s]", collegeId);
        return collegeId;
    }

    public void deleteCollege(String collegeName) throws ApiException {
        DeleteInstitutionEntityPayLoad body = new DeleteInstitutionEntityPayLoad();
        int collegeId = getCollegeIdByName(collegeName);
        body.setId(collegeId);
        writeRequestParamsToLog(body);
        cmsCollegeApi.cmsDeleteCollege(token, body);
    }
}
