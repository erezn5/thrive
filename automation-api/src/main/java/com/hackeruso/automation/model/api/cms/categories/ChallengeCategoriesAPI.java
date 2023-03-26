package com.hackeruso.automation.model.api.cms.categories;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.pages.cms.Status;
import com.hackeruso.automation.model.pages.cms.StatusMode;
import com.hackeruso.automation.utils.FileUtil;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsCategoriesApi;
import io.swagger.client.model.DataWithUUID;
import io.swagger.client.model.GetTableWithUUIDResponse;
import io.swagger.client.model.StatusBodyStringReq;
import io.swagger.client.model.StatusChallengeResponse;

import java.io.File;

public class ChallengeCategoriesAPI extends BaseAPI {
    private final String LION_IMAGE_FILE_PATH = EnvConf.getProperty("automation.lion.file.path.location");

    private final CmsCategoriesApi cmsCategoriesApi = new CmsCategoriesApi();
    public ChallengeCategoriesAPI(String host, String token) {
        super(host, token);
        cmsCategoriesApi.setApiClient(restApi.getClient());
    }

    public void changeChallengeCategoryStatusModeByName(String challengeCategoryName, StatusMode statusMode) throws ApiException {
        DataWithUUID item = getCategoryItemByName(challengeCategoryName);

        cmsCategoriesApi.changeChallengeCategoryStatusMode(token, challengeCategoryName, item.getId(),
                item.getDescription(), item.getStatus(), statusMode.getValue(), item.getSlug());
    }

    public String getChallengeCategoryIdByName(String challengeCategoryName) throws ApiException {
        return getCategoryItemByName(challengeCategoryName).getId();
    }

    public DataWithUUID getCategoryItemByName(String challengeCategoryName) throws ApiException {
        return getCategoriesList(250, 1, "", "", true, challengeCategoryName, "name").getData().get(0);
    }

    public GetTableWithUUIDResponse getCategoriesList(int resultsPerPage, int page, String status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        return cmsCategoriesApi.getCategories(token, resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
    }

    public void createCategory(String categoryName, String categoryDesc, String slug, String minPoints, int minLevel, int status) throws ApiException {
        cmsCategoriesApi.createNewCategory(token, new File(FileUtil.getFile(LION_IMAGE_FILE_PATH)),categoryName,categoryDesc,slug,minPoints,minLevel, status);
    }

    public StatusChallengeResponse editChallengeCategory(String challengeCategoryName, Status status, boolean statusMode) throws ApiException {
        String challengeCategoryId = getChallengeCategoryIdByName(challengeCategoryName);
        String model = "ChallengeCategory";
        int statusApi = status.apiStatus;
        StatusBodyStringReq statusBodyStringReq = new StatusBodyStringReq();

        statusBodyStringReq.setStatus(statusApi);
        statusBodyStringReq.setModel(model);
        statusBodyStringReq.setId(challengeCategoryId);
        statusBodyStringReq.setStatusModeManual(statusMode);

        printBody(statusBodyStringReq);

        StatusChallengeResponse response =  cmsCategoriesApi.changeChallengeCategoryStatus(token, statusBodyStringReq);
        printResponse(response);
        return response;
    }
}
