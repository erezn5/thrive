package com.hackeruso.automation.model.api.cms.certifications;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.cms.challenges.ChallengesAPI;
import com.hackeruso.automation.model.api.cms.institutions.ClassesAPI;
import com.hackeruso.automation.model.pages.cms.LevelItem;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsCertificationApi;
import io.swagger.client.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class CertificationsAPI extends BaseAPI {

    private static final CmsCertificationApi cmsCertificationApi= new CmsCertificationApi();
    private final ChallengesAPI challengesAPI;
    private final ClassesAPI classesAPI;
    public CertificationsAPI(String host, String token) {
        super(host, token);
        cmsCertificationApi.setApiClient(restApi.getClient());
        challengesAPI = new ChallengesAPI(host, token);
        classesAPI = new ClassesAPI(host, token);
    }

    public void createNewCertificate(String name, String description, int expired, String... challengeNames) throws ApiException {
        CreateCertificationBody body = new CreateCertificationBody();
        List<String> challengesId = new ArrayList<>();

        body.setName(name);
        body.setDescription(description);
        body.setExpiredAfter(expired);

        for(String challengeName : challengeNames){
            challengesId.add(challengesAPI.getChallengeIdByName(challengeName));
        }

        body.setContents(challengesId);
        body.setStatus(1);
        cmsCertificationApi.createCertificate(token, body);
    }

    public void attachChallengesToCertification(String certificationName, List<String> challengeNameLst) throws ApiException {
        CertificationAttachChallengesBodyRequest body =  new CertificationAttachChallengesBodyRequest();

        int certificationId = getCertificationIdByName(certificationName);

        CertificationItemResponse itemResponse = getSpecificCertificateInfo(certificationName);
        int year = itemResponse.getExpiredAfter();

        body.setName(certificationName);
        body.setDescription("Test description");
        body.setId(certificationId);
        body.setYear(year);

        List<CertificationAssignedContent> assignedCertContentList = setAssignedCertContent(challengeNameLst);
        assignedCertContentList.addAll(setOriginalCertContent(certificationName));
        body.setAssignedChallenges(assignedCertContentList);

        cmsCertificationApi.attachChallengesToCertification(token, certificationId, body);
    }

    public void attachClassesToCertification(String certificationName, List<String> challengeNameLst, String... classNamesLst) throws ApiException {
        CertificationAttachBody body = new CertificationAttachBody();

        int certificationId = getCertificationIdByName(certificationName);

        List<Integer> classesIdLst = new ArrayList<>();
        for(String className : classNamesLst){
            int classId = classesAPI.getClassIdByName(className);
            classesIdLst.add(classId);
        }
        Log.i("Setting classes to certificate");

        //region //challenges that were there originally
        List<CertificationAssignedContent> originalCertContentLst = setOriginalCertContent(certificationName);
        Log.i("Restoring and setting existed challenges to certificate");
        //endregion

        //region //setting new challenges
        List<CertificationAssignedContent> assignedCertContentLst = setAssignedCertContent(challengeNameLst);
        Log.i("Restoring and setting new challenges to certificate");
        //endregion

        body.setClassesIds(classesIdLst);

        body.setClassesDetachIds(Collections.emptyList());
        body.setAssignedCertContents(assignedCertContentLst); //new challenges to assign
        body.setOriginalCertContents(originalCertContentLst);

        cmsCertificationApi.attachClassesToCertification(token, certificationId, body);
    }

    private List<CertificationAssignedContent> setOriginalCertContent(String certificationName) throws ApiException {

        CertificationItemResponse certificationItemResponse = getSpecificCertificateInfo(certificationName);
        List<CertificationContentItem> kst = certificationItemResponse.getContents();

        List<CertificationAssignedContent> originalCertContentLst = new ArrayList<>();
        for(CertificationContentItem certificationContentItem : kst){
            String challengeName = certificationContentItem.getChallengeContentInfoWithCategory().getName();
            String challengeId = certificationContentItem.getChallengeContentInfoWithCategory().getId();
            String challengeCategoryName = certificationContentItem.getChallengeContentInfoWithCategory().getCategory().getName();
            int level = LevelItem.getApiValueByLevelText(challengesAPI.getChallengeItemInfoByName(certificationContentItem.getChallengeContentInfoWithCategory().getName()).getLevel());

            CertificationAssignedContent originalCertContent = new CertificationAssignedContent();
            originalCertContent.setName(challengeName);
            originalCertContent.setId(challengeId);
            originalCertContent.setCategory(challengeCategoryName);
            originalCertContent.setLevel(level);

            originalCertContentLst.add(originalCertContent);
        }
        return originalCertContentLst;
    }

    private List<CertificationAssignedContent> setAssignedCertContent(List<String>challengeNameLst) throws ApiException {
        List<CertificationAssignedContent> assignedCertContentLst = new ArrayList<>();
        for(String challengeName : challengeNameLst) {
            int level = LevelItem.getApiValueByLevelText(challengesAPI.getChallengeItemInfoByName(challengeName).getLevel());

            String challengeCategory = challengesAPI.getChallengeCategory(challengeName);
            String id = challengesAPI.getChallengeIdByName(challengeName);

            CertificationAssignedContent assignedCertContent = new CertificationAssignedContent();
            assignedCertContent.setCategory(challengeCategory);
            assignedCertContent.setId(id);
            assignedCertContent.setName(challengeName);
            assignedCertContent.setLevel(level);
            assignedCertContentLst.add(assignedCertContent);
        }

        return assignedCertContentLst;
    }

    public int getCertificationIdByName(String certificationName) throws ApiException {
        CertificationListResponse certificationListResponse = getCertificationList();
        return certificationListResponse.stream().filter(item -> item.getName().equals(certificationName)).findFirst().map(CertificationListResponseInner::getId).orElse(0);
    }

    public CertificationListResponse getCertificationList() throws ApiException {
        return cmsCertificationApi.getCertificationList(token, 15, 1);
    }

    public CertificationItemResponse getSpecificCertificateInfo(String certificationName) throws ApiException {
        int certificationId = getCertificationIdByName(certificationName);
        return cmsCertificationApi.getCertificationInfo(token, certificationId);
    }

    public void updateCertificateStatus(String certificationName, boolean status) throws ApiException {
        int certificationId = getCertificationIdByName(certificationName);
        CertificationStatusRequest body = new CertificationStatusRequest();
        body.setStatus(status);
        cmsCertificationApi.changeCertificationStatus(token, certificationId, body);
    }
}
