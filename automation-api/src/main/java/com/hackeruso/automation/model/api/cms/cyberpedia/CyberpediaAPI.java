package com.hackeruso.automation.model.api.cms.cyberpedia;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.pages.cms.LevelItem;
import com.hackeruso.automation.model.pages.cms.Status;
import com.hackeruso.automation.model.pages.cms.StatusMode;
import com.hackeruso.automation.utils.FileUtil;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsCyberpediaApi;
import io.swagger.client.model.*;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CyberpediaAPI extends BaseAPI {

    private static final CmsCyberpediaApi cmsCyberpediaApi = new CmsCyberpediaApi();

    public CyberpediaAPI(String host, String token) {
        super(host, token);
        cmsCyberpediaApi.setApiClient(restApi.getClient());
    }

    public void createNewCyberpediaCategory(String name, String desc, String fileName, Status status) throws ApiException {
        cmsCyberpediaApi.createCyberpediaCategory(token, new File(FileUtil.getFile(fileName)), name, desc, status.apiStatus);
    }

    public String updatePediaCategoryStatusMode(String categoryName, StatusMode statusMode) throws ApiException {
        List<DataWithUUID> categoryItem = getCyberpediaCategoriesList(categoryName);
        return cmsCyberpediaApi.editPediaCategoryStatusMode(token, categoryItem.get(0).getName(),
                categoryItem.get(0).getId(), categoryItem.get(0).getDescription(), categoryItem.get(0).getStatus(), statusMode.getValue());
    }

    public DataWithUUID getCyberpediaCategoryDataByName(String cyberpediaCategoryName) throws ApiException {
        List<DataWithUUID> lst = getCyberpediaCategoriesList(cyberpediaCategoryName);
        return lst.stream().filter(item->item.getName().equals(cyberpediaCategoryName)).findFirst().orElse(null);
    }

    public String getCyberpediaCategoryIdByName(String categoryName) throws ApiException {
        List<DataWithUUID> lst = getCyberpediaCategoriesList(categoryName);
        return lst.get(0).getId();
    }

    public void createNewCyberpediaTerm(String fileName, String categoryName, String termName, Status status, String content, String link) throws ApiException {
        String categoryId = getCyberpediaCategoryIdByName(categoryName);
        if (fileName == null) {
            cmsCyberpediaApi.createCyberpediaTerm(token, null, categoryId, termName, status.apiStatus, content, link);
        } else {
            cmsCyberpediaApi.createCyberpediaTerm(token, new File(FileUtil.getFile(fileName)), categoryId, termName, status.apiStatus, content, link);
        }
    }

    public void changePediaCategoryStatus(String categoryName, Status status, boolean statusMode) throws ApiException {
        String categoryId = getCyberpediaCategoryIdByName(categoryName);

        StatusBodyStringReq statusBodyStringReq = new StatusBodyStringReq();

        statusBodyStringReq.setId(categoryId);
        statusBodyStringReq.setStatus(status.apiStatus);
        statusBodyStringReq.setStatusModeManual(statusMode);

        printBody(statusBodyStringReq);

        String response = cmsCyberpediaApi.editPediaCategoryStatus(token, statusBodyStringReq);
        printResponse(response);
    }

    public void changeQuizStatus(String quizName, Status status) throws ApiException {
        int quizId = getQuizDataByName(quizName).getId();

        StatusBodyNumberReq bodyReq = setBodyRequestForChangingStatus(status, quizId);
        printBody(bodyReq);

        String response = cmsCyberpediaApi.editQuizStatus(token, bodyReq);
        printResponse(response);
    }

    public void changeTermStatus(String termName, Status status) throws ApiException {
        int termId = getTermDataByName(termName).getId();

        StatusBodyNumberReq bodyReq = setBodyRequestForChangingStatus(status, termId);
        printBody(bodyReq);

        StatusResponse response = cmsCyberpediaApi.editTermStatus(token, bodyReq);
        printResponse(response);
    }

    private StatusBodyNumberReq setBodyRequestForChangingStatus(Status status, int quizId) {
        StatusBodyNumberReq bodyReq = new StatusBodyNumberReq();
        bodyReq.setId(quizId);
        bodyReq.setStatusModeManual(false);
        bodyReq.setStatus(status.apiStatus);
        printBody(bodyReq);
        return bodyReq;
    }

    public List<QuizDataResponse> getQuizList() throws ApiException {
        return cmsCyberpediaApi.getQuizzesList(token, 250, 1, "", "", true, "", "").getData();
    }

    public List<Data> getTermsList() throws ApiException {
        return cmsCyberpediaApi.getTermsList(token, 300, 1, "", "", true, "", "").getData();
    }

    private Data getDataFromList(String name, List<Data> lst) {
        return lst.stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
    }

    private QuizDataResponse getDataUUIDFromList(String name, List<QuizDataResponse> lst) {
        return lst.stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
    }

    public QuizDataResponse getQuizDataByName(String quizName) throws ApiException {
        return getDataUUIDFromList(quizName, getQuizList());
    }

    public Data getTermDataByName(String termName) throws ApiException {
        return getDataFromList(termName, getTermsList());
    }

    public List<DataWithUUID> getCyberpediaCategoriesList(String categoryName) throws ApiException { //will get the data for the categories and will not contain other common data info
        return cmsCyberpediaApi.getCyberpediaCategoriesList(token, 250, 1, "", "", true, categoryName, categoryName).getData();
    }

    public void deleteCategory(String categoryName) throws ApiException {
        String id = getCyberpediaCategoryIdByName(categoryName);
        DeleteCategoryPayLoad payload = new DeleteCategoryPayLoad();
        payload.setMode("Delete");
        payload.setData(Collections.singletonList(id));
        printBody(payload);
        cmsCyberpediaApi.deleteCyberpediaCategory(token, payload);
    }

    public void createQuiz(String quizName, String description, Status status, String categoryName, LevelItem level, String imageFilePath,
                           String question0Content, String questions0Answers0Content, String questions0Answers1Content, String question0CorrectAnswer) throws ApiException {
        String categoryId = getCyberpediaCategoryIdByName(categoryName);
        cmsCyberpediaApi.createCyberpediaQuiz(token, quizName, description, status.apiStatus, categoryId, level.apiValue, new File(FileUtil.getFile(imageFilePath)),
                question0Content, questions0Answers0Content, questions0Answers1Content, question0CorrectAnswer);
    }

    public void deleteTerms(String termName) throws ApiException {
        List<Data> lstTerms = getTermsList();
        int termId = Objects.requireNonNull(lstTerms.stream().filter(item -> item.getName().equals(termName)).findFirst().orElse(null)).getId();
        DeletePediaAssetsData payload = new DeletePediaAssetsData();
        payload.setMode("Delete");
        payload.setData(Collections.singletonList(termId));
        printBody(payload);
        cmsCyberpediaApi.deleteCyberpediaTerm(token, payload);
    }

    public void deleteQuiz(String name) throws ApiException {
        List<QuizDataResponse> quizzesLst = getQuizList();
        int quizId = Objects.requireNonNull(quizzesLst.stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null)).getId();
        DeletePediaAssetsData payload = new DeletePediaAssetsData();
        payload.setMode("Delete");
        payload.setData(Collections.singletonList(quizId));
        printBody(payload);
        cmsCyberpediaApi.deleteCyberpediaQuiz(token, payload);
    }
}