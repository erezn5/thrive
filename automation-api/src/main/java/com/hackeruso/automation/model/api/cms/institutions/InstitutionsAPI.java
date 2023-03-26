package com.hackeruso.automation.model.api.cms.institutions;

import com.hackeruso.automation.model.api.BaseAPI;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsInstitutionApi;
import io.swagger.client.model.*;

import java.util.List;

public class InstitutionsAPI extends BaseAPI {
    CmsInstitutionApi institutionApi = new CmsInstitutionApi();
    public InstitutionsAPI(String host, String token) {
        super(host, token);
        institutionApi.setApiClient(restApi.getClient());
    }

    public List<DataInstitutions> getInstitutionsList(int resultsPerPage, int page, int status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        GetTableInstitutionsResponse getTable = institutionApi.getInstittutions(token, resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
        writeRequestParamsToLog(getTable);
        return getTable.getData();
    }

    public int getInstitutionIdByName(String name) throws ApiException {
        List<DataInstitutions> institutionsList = getInstitutionsList(10, 1, 1, "", true, name, name);
        return institutionsList.get(0).getId();
    }

    public List<Data> getBusinessesList(int resultsPerPage, int page, String status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        GetTableResponse getTable = institutionApi.getBusinesses(token, resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
        writeRequestParamsToLog(getTable);
        return getTable.getData();
    }

    public SyllabiListResponse getListOfSyllabi() throws ApiException {
        return institutionApi.getSyllabiList(token);
    }

    /**
     *
     * @param institutionName - name of the institution for creation
     * status - active - 1, inactive - 0 //for setStatus method
     * setBusinessType - 1=B2C // 2=B2B
     * setGracePeriod - grace period 90 days for default
     * @return
     * @throws ApiException
     */

    public String createNewInstitution(String institutionName) throws ApiException {
        NewInstitutionPayload newInstitutionPayload = new NewInstitutionPayload();
        newInstitutionPayload.setName(institutionName);
        newInstitutionPayload.setStatus("1");
        newInstitutionPayload.setBusinessType("1");
        newInstitutionPayload.setGracePeriod("90");
        writeRequestParamsToLog(newInstitutionPayload);
        return institutionApi.cmsCreateInstitution(token, newInstitutionPayload);
    }

    public void deleteInstitution(String institutionName) throws ApiException {
        DeleteInstitutionEntityPayLoad bodyPayload = new DeleteInstitutionEntityPayLoad();
        int institutionId = getInstitutionIdByName(institutionName);
        bodyPayload.setId(institutionId);
        writeRequestParamsToLog(bodyPayload);
        institutionApi.cmsDeleteInstitution(token, bodyPayload);
    }
}
