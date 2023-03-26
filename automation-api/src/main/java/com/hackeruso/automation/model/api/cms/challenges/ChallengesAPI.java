package com.hackeruso.automation.model.api.cms.challenges;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.cms.CmsPageStatus;
import com.hackeruso.automation.model.pages.cms.Status;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsChallengesApi;
import io.swagger.client.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChallengesAPI extends BaseAPI {

    private static final CmsChallengesApi cmsChallengesApi = new CmsChallengesApi();
    public ChallengesAPI(String host, String token) {
        super(host, token);
        cmsChallengesApi.setApiClient(restApi.getClient());
    }

    public String getFirstActiveChallengeName() throws ApiException {
        List<DataWithUUID> challengeLst = getChallengesList(CmsPageStatus.ACTIVE, 250, 1, "", "", true, "", "name");
        return challengeLst.get(0).getName();
    }

    public String getInactiveChallengeIdByName(String challengeName) throws ApiException {
        List<DataWithUUID> challengeLst = getChallengesList(CmsPageStatus.INACTIVE, 250, 1, "", "", true, challengeName, "name");
        return Objects.requireNonNull(challengeLst.stream()
                .filter(item -> item.getName().equals(challengeName)).findFirst().orElse(null)).getId();
    }

    public String getChallengeIdByName(String challengeName) throws ApiException {
        List<DataWithUUID> challengeLst = getChallengesList(CmsPageStatus.ACTIVE, 250, 1, "", "", true, challengeName, "name");
        if(challengeLst.size()==0){
            challengeLst = getChallengesList(CmsPageStatus.INACTIVE, 250, 1, "", "", true, challengeName, "name");
        }
        return Objects.requireNonNull(challengeLst.stream()
                .filter(item -> item.getName().equals(challengeName)).findFirst().orElse(null)).getId();
    }

    public CheckerResponse checkerVerifier(String challengeName, String challengeFlagId) throws ApiException {
        String challengeId = getChallengeIdByName(challengeName);

        CheckerPayload checkerPayload = new CheckerPayload();

        List<CheckerItemPayload> checkerItemPayloadList = new ArrayList<>();

        CheckerItemPayload checkerItemPayload = new CheckerItemPayload();

        checkerItemPayload.setChallengeId(challengeId);
        checkerItemPayload.setId(1126);
        checkerItemPayload.setPlaceholder(".");
        checkerItemPayload.setAnswer(challengeFlagId);

        checkerItemPayloadList.add(checkerItemPayload);

        checkerPayload.setInputs(checkerItemPayloadList);
        printBody(checkerPayload);

        CheckerResponse checkerResponse =  cmsChallengesApi.challengeChecker(token, challengeId, checkerPayload, challengeName);
        printResponse(checkerResponse);
        return checkerResponse;
    }

    public List<DataWithUUID> getChallengesList(CmsPageStatus cmsPageView, int resultsPerPage, int page, String status, String sortBy, boolean sortOrder, String searchValue, String searchBy) throws ApiException {
        GetTableWithUUIDResponse response = cmsChallengesApi.getChallenges(token, cmsPageView.getIndex(), resultsPerPage, page, status, sortBy, sortOrder, searchValue, searchBy);
        return response.getData();
    }

    //This is the API we are using when we are in the website
    public DetailedChallengesInfoResponse getDetailedChallengesList() throws ApiException {
        return cmsChallengesApi.getDetailedChallengesInfo(token, "challenges", true);
    }

    public DataWithUUID getChallengeItemInfoByName(String challengeName) throws ApiException {
        List<DataWithUUID> challengeLst = getChallengesList(CmsPageStatus.ACTIVE, 250, 1, "", "", true, challengeName, "name");
        return challengeLst.stream().filter(item->item.getName().equals(challengeName)).findFirst().orElse(null);
    }

    public ChallengeDetailedItem getSpecificDetailedChallengeData(String challengeName) throws ApiException {
        DetailedChallengesInfoResponse detailedChallengesInfoResponse = getDetailedChallengesList();
        return detailedChallengesInfoResponse.getChallenges().stream().filter(item->item.getChallengeName().equals(challengeName)).findFirst().orElse(null);
    }

    public String getChallengeCategory(String challengeName) throws ApiException {
        return getChallengeCategory(CmsPageStatus.ACTIVE, challengeName);
    }

    public String getChallengeCategory(CmsPageStatus pageStatus, String challengeName) throws ApiException {
        return getChallengeDataByName(pageStatus, challengeName).getCategory();
    }

    private DataWithUUID getChallengeDataByName(CmsPageStatus pageStatus, String challengeName) throws ApiException {
        List<DataWithUUID> challengeLst = getChallengesList(pageStatus, 250, 1, "", "", true, challengeName, "name");
        return challengeLst.stream().filter(item -> item.getName().equals(challengeName)).findFirst().orElse(null);
    }

    public int getChallengeVotes(String challengeName) throws ApiException {
        return getChallengeDataByName(CmsPageStatus.ACTIVE, challengeName).getVotes();
    }

    public StatusResponse changeChallengeStatus(String challengeName, Status status) throws ApiException {
        String challengeId;
        DataWithUUID dataWithUUID = getChallengeDataByName(CmsPageStatus.INACTIVE, challengeName);
        if (status.equals(Status.INACTIVE) && dataWithUUID != null) {
            challengeId = getInactiveChallengeIdByName(challengeName);
        } else {
            challengeId = getChallengeIdByName(challengeName);
        }
        return getCommonChallengeStatusResponse(status, challengeId);
    }

    private StatusResponse getCommonChallengeStatusResponse(Status status, String challengeId) throws ApiException {
        StatusBodyStringReq bodyReq = new StatusBodyStringReq();
        bodyReq.setId(challengeId);
        bodyReq.setModel("Challenge");
        bodyReq.setStatusModeManual(false);
        bodyReq.setStatus(status.apiStatus);
        printBody(bodyReq);

        StatusResponse response = cmsChallengesApi.changeChallengeStatus(token, bodyReq);
        printResponse(response);
        return response;
    }
}
