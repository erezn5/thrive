package com.hackeruso.automation.model.api.cms.practice_arena;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.logger.LoggerFormat;
import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.cms.challenges.ChallengesAPI;
import com.hackeruso.automation.model.api.cms.content_manager.ContentManagerAPI;
import com.hackeruso.automation.model.api.cms.cyberpedia.CyberpediaAPI;
import com.hackeruso.automation.model.api.db.SQL;
import com.hackeruso.automation.model.infra_components.SkillItems;
import com.hackeruso.automation.model.pages.cms.Status;
import com.hackeruso.automation.model.pages.cms.StatusMode;
import com.hackeruso.automation.model.pages.cms.practice_arena.modules.create_edit_module.general_information_and_materials.Team;
import com.hackeruso.automation.model.pages.cms.practice_arena.modules.create_edit_module.general_information_and_materials.UserLevel;
import com.hackeruso.automation.utils.FileUtil;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsContentManagerApi;
import io.swagger.client.api.CmsPracticeArenaApi;
import io.swagger.client.model.*;
import org.awaitility.Duration;

import java.io.File;
import java.util.*;

public class PracticeArenaAPI extends BaseAPI {
    public static final LoggerFormat Log = new LoggerFormat();
    private static final CmsPracticeArenaApi practiceArenaApi = new CmsPracticeArenaApi();
    private final ContentManagerAPI contentManagerAPI;
    private final CyberpediaAPI cyberpediaAPI;
    private final ChallengesAPI challengesAPI;

    private static final String UNICORN_ICON_FILE_PATH = EnvConf.getProperty("automation.unicorn.icon.file.path.location");

    public PracticeArenaAPI(String host, String token) {
        super(host, token);
        practiceArenaApi.setApiClient(restApi.getClient());
        contentManagerAPI = new ContentManagerAPI(host, token);
        cyberpediaAPI = new CyberpediaAPI(host, token);
        challengesAPI = new ChallengesAPI(host, token);
    }

    public ModuleItemData changeModuleStatus(String moduleName, String lessonName, String moduleDescription, StatusMode statusMode, String createdByEmail, String updatedByEmail, Status... status) throws Exception {
        ModuleItemData moduleItemResponse = getPAModules().stream().filter(item -> item.getName().equals(moduleName)).findFirst().orElse(null);
        assert moduleItemResponse != null;

        String syllabiName = moduleItemResponse.getSyllabi().isEmpty() ? "" : moduleItemResponse.getSyllabi().get(0);
        int moduleStatus = status.length != 0 ? status[0].apiStatus : moduleItemResponse.getStatus();
        SQL sql = new SQL();
        String createdBy = sql.getQuerySingleValue(String.format("select name from galex_cyber.users where email='%s';", createdByEmail));
        String updatedBy = sql.getQuerySingleValue(String.format("select name from galex_cyber.users where email='%s';", updatedByEmail));
        syllabiName = syllabiName.isEmpty() ? "" : sql.getQuerySingleValue(String.format("select id from galex_cyber.syllabi where name='%s';", syllabiName));
        sql.closeDBConnection();

        int order = 1;
        int moduleContent0Order = 1;
        String moduleContent1Order = "1";
        String moduleContent2Order = "1";

        ModuleItemData res = practiceArenaApi.changeModuleStatus(token, moduleItemResponse.getId(), moduleItemResponse.getId(), moduleItemResponse.getSyllabusId(), 1, moduleItemResponse.getLevel(),
                moduleName, moduleDescription, moduleItemResponse.getSlug(), moduleItemResponse.getIcon(), order, String.valueOf(statusMode.getValue()),
                getLessonIdByLessonName(lessonName), moduleContent0Order, lessonName,
                SkillItems.SMTP.getSkillId(), moduleContent1Order, "achieved skill",
                SkillItems.APK.getSkillId(), moduleContent2Order, "prerequisite skill",
                moduleStatus,
                moduleItemResponse.getCreatedAt(), moduleItemResponse.getUpdatedAt(), createdBy, updatedBy,
                syllabiName, Collections.emptyList(),
                SkillItems.SMTP.getValue(), SkillItems.SMTP.getSkillId(), true, "achieved",
                SkillItems.APK.getValue(), SkillItems.APK.getSkillId(), true, "prerequisite",
                getLessonIdByLessonName(lessonName));

        printResponse(res);
        return res;
    }

    public LessonItemDataResponse getSpecificLessonDataByName(String lessonName) throws ApiException {
        return getPALessons().stream().filter(item -> item.getName().equals(lessonName)).findFirst().orElse(null);
    }

    public String getLabIdByName(String labName) throws ApiException {
        LabListResponse labListResponse = getPALabs();
        return Objects.requireNonNull(labListResponse.stream()
                .filter(labItem -> labItem.getName().equals(labName)).findFirst().orElse(null)).getId();
    }

    public LabDataResponse getSpecificLabByName(String labName) throws ApiException {
        String labId = getLabIdByName(labName);
        return practiceArenaApi.getSpecificLab(token, labId);
    }

    //TODO - create a ticket to groom the following methods: setLessonContent, setLabTopicToLesson, setLessonItemDataRequest TDP-1937
    public void setLessonContent(String lessonName, String moduleName, List<String> termNames, List<String> quizzesNames, Status status) throws ApiException {
        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataRequest lessonItemDataRequest = getLessonItemDataRequest(lessonId, moduleName, status);

        List<LessonItemData> lessonContentItemDataLst = new ArrayList<>();
        //region 'List<LessonItemData>'
        for (String quizName: quizzesNames) {
            int quizId = cyberpediaAPI.getQuizDataByName(quizName).getId();
            LessonItemData lessonItemDataQuiz = new LessonItemData();
            lessonItemDataQuiz.setContentId(quizId);
            lessonItemDataQuiz.setContentTypeName("quiz");
            lessonItemDataQuiz.setName(quizName);

            lessonContentItemDataLst.add(lessonItemDataQuiz);
        }

        for (String termName : termNames) {
            int termId = cyberpediaAPI.getTermDataByName(termName).getId();
            LessonItemData lessonItemDataTerm = new LessonItemData();
            lessonItemDataTerm.setContentId(termId);
            lessonItemDataTerm.setContentTypeName("term");
            lessonItemDataTerm.setName(termName);

            lessonContentItemDataLst.add(lessonItemDataTerm);
        }
        //endregion
        lessonItemDataRequest.setLessonContent(lessonContentItemDataLst);

        setModuleToLessonItemDataRequest(moduleName, lessonItemDataRequest);

        LessonEditResponse response = practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
        printResponse(response);

        //fixme: until Swagger 3.0
        //region 'for Challenge'
//        String challengeId = challengesAPI.getChallengeIdByName(challengeName);
//        LessonItemData lessonItemDataChallenge = new LessonItemData();
//        lessonItemDataChallenge.setName(challengeName);
//        lessonItemDataChallenge.setContentTypeName("challenge");
//        lessonItemDataChallenge.setContentId(challengeId);
        //endregion
    }

    public void setLabTopicToLesson(String lessonName, String moduleName, List<String> labNames, String... topicNames) throws ApiException {
        List<TopicItemDataRequest> topics = getTopicItemDataAsList(labNames, topicNames);
        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataRequest lessonItemDataRequest = getLessonItemDataRequest(lessonId, moduleName, Status.ACTIVE);
        lessonItemDataRequest.setTopics(topics);
        practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
    }

    public void setHomeworkToLesson(String lessonName, String moduleName, String homework) throws ApiException {
        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataRequest lessonItemDataRequest = getLessonItemDataRequest(lessonId, moduleName);
        lessonItemDataRequest.setHomework(homework);
        practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
    }

    public void setSummaryToLesson(String lessonName, String moduleName, List<String> summaryItemsList, String contentType) throws ApiException {
        List<LessonSummaryData> lessonSummaryDataList = new ArrayList<>();
        LessonSummaryData lessonSummaryData = new LessonSummaryData();
        int order = 1;

        for (String itemName : summaryItemsList) {
            lessonSummaryData.setContentId(getLessonSummaryContentId(itemName, contentType));
            lessonSummaryData.setContentType(contentType);
            lessonSummaryData.setOrder(order);
            order++;
            lessonSummaryDataList.add(lessonSummaryData);
        }

        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataRequest lessonItemDataRequest = getLessonItemDataRequest(lessonId, moduleName);
        lessonItemDataRequest.setSummary(lessonSummaryDataList);
        practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
    }

    private int getLessonSummaryContentId(String itemName, String contentType) throws ApiException {
        int contentId;

        switch (contentType) {
            case "Term":
                contentId = cyberpediaAPI.getTermDataByName(itemName).getId();
                break;
            case "Quiz":
                contentId = cyberpediaAPI.getQuizDataByName(itemName).getId();
                break;
            default:
                contentId = -1;
        }

        return contentId;
    }

    private LessonItemDataRequest getLessonItemDataRequest(String lessonId, String moduleName, Status... status) throws ApiException {
        LessonItemDataResponse lessonItemDataResponse = practiceArenaApi.getSingleLessonData(token, lessonId);
        if(status.length > 0)
            lessonItemDataResponse.setStatus(status[0].apiStatus);
        return setLessonItemDataRequest(lessonId, moduleName, lessonItemDataResponse);
    }

    private List<TopicItemDataRequest> getTopicItemDataAsList(List<String> labNames, String... topicNames) throws ApiException {
        List<TopicItemDataRequest> topics = new ArrayList<>();
        TopicItemDataRequest topicItemDataRequest;
        List<AssetItemData> assetItemDataList;

        for (int i = 0; i < labNames.size(); ++i) {
            assetItemDataList = new ArrayList<>();
            String labName = labNames.get(i);
            topicItemDataRequest = new TopicItemDataRequest();
            topicItemDataRequest.setOrder(i + 1);
            if(topicNames.length!=0){
                topicItemDataRequest.setName(topicNames[i]);
            }else {
                topicItemDataRequest.setName(labName);
            }
            LabDataResponse labDataResponse = getSpecificLabByName(labName);
            AssetItemData assetItemData = new AssetItemData();
            assetItemData.setContentId(labDataResponse.getLab().getId());
            assetItemData.setName(labDataResponse.getLab().getName());
            assetItemData.setContentTypeId(13);
            assetItemData.setContentTypeName("lab");
            assetItemDataList.add(assetItemData);
            topicItemDataRequest.setAssets(assetItemDataList);
            topics.add(topicItemDataRequest);
        }

        return topics;
    }

    private LessonItemDataRequest setLessonItemDataRequest(String lessonId, String moduleName, LessonItemDataResponse lessonItemResponse, StatusMode... statusMode) throws ApiException {
        LessonItemDataRequest lessonItemDataRequest = new LessonItemDataRequest();

        lessonItemDataRequest.setSequence(lessonItemResponse.getSequence());
        lessonItemDataRequest.setId(lessonItemResponse.getId());
        lessonItemDataRequest.syllabusId(lessonItemResponse.getSyllabusId());
        lessonItemDataRequest.setName(lessonItemResponse.getName());
        lessonItemDataRequest.setCode(lessonItemResponse.getCode());
        lessonItemDataRequest.setDescription(lessonItemResponse.getDescription());
        lessonItemDataRequest.setStatus(lessonItemResponse.getStatus());
        lessonItemDataRequest.setVersion(lessonItemResponse.getVersion());
        lessonItemDataRequest.setHomework(lessonItemResponse.getHomework());
        lessonItemDataRequest.setCreatedAt(lessonItemResponse.getCreatedAt());
        lessonItemDataRequest.setUpdatedAt(lessonItemResponse.getUpdatedAt());
        lessonItemDataRequest.setUpdatedBy(lessonItemResponse.getUpdatedBy());
        lessonItemDataRequest.setCreatedBy(lessonItemResponse.getCreatedBy());

        if (statusMode.length != 0) {
            lessonItemDataRequest.setStatusMode(statusMode[0].getValue());
        } else {
            lessonItemDataRequest.setStatusMode(lessonItemResponse.getStatusMode());
        }

        lessonItemDataRequest.setHomeworks(lessonItemResponse.getHomeworks());
        lessonItemDataRequest.setIntroduction(new ArrayList<>());
        lessonItemDataRequest.setSummary(new ArrayList<>());

        if (!moduleName.isEmpty()) {
            setModuleToLessonItemDataRequest(moduleName, lessonItemDataRequest);
        }

        lessonItemDataRequest.setLessonContent(setLessonContentData(lessonItemResponse.getLessonContent()));
        lessonItemDataRequest.setUuid(lessonId);
        lessonItemDataRequest.setSummaryKeyTakeAway("");

        printBody(lessonItemDataRequest);

        return lessonItemDataRequest;
    }

    private void setModuleToLessonItemDataRequest(String moduleName, LessonItemDataRequest lessonItemDataRequest) throws ApiException {
        ModuleItemData itemModuleDataRes;
        ArrayList<ModuleItemData> moduleItemLst = getPAModules();
        itemModuleDataRes = moduleItemLst.stream().filter(moduleItem -> moduleItem.getName().equals(moduleName)).findFirst().orElse(null);
        ModuleItemDataRequest moduleItemDataReq = new ModuleItemDataRequest();
        assert itemModuleDataRes != null;
        moduleItemDataReq.setId(itemModuleDataRes.getId());
        moduleItemDataReq.setName(itemModuleDataRes.getName());
        moduleItemDataReq.setSyllabusId(itemModuleDataRes.getSyllabusId());
        lessonItemDataRequest.setModules(Collections.singletonList(moduleItemDataReq));
    }

    /*
    //TODO: Need validate if this method need!!!
    public void editLessonWithLessonContent(String lessonName, String moduleName, Status status, String quizName, String termName) throws ApiException {
        List<LessonItemData> lessonItemDataLst = new ArrayList<>();
        LessonItemData lessonItemDataQuiz = new LessonItemData();
        int quizId = cyberpediaAPI.getQuizDataByName(quizName).getId();
        lessonItemDataQuiz.setContentId(quizId);
        lessonItemDataQuiz.setContentTypeName("quiz");
        lessonItemDataQuiz.setName(quizName);
        lessonItemDataLst.add(lessonItemDataQuiz);

        LessonItemData lessonItemDataTerm = new LessonItemData();
        int termId = cyberpediaAPI.getTermDataByName(termName).getId();
        lessonItemDataTerm.setContentId(termId);
        lessonItemDataTerm.setContentTypeName("term");
        lessonItemDataTerm.setName(termName);
        lessonItemDataLst.add(lessonItemDataTerm);

        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataResponse item = practiceArenaApi.getSingleLessonData(token, lessonId);
        LessonItemDataRequest lessonItemDataRequest = new LessonItemDataRequest();
        lessonItemDataRequest.setSequence(item.getSequence());
        lessonItemDataRequest.setId(item.getId());
        lessonItemDataRequest.syllabusId(item.getSyllabusId());
        lessonItemDataRequest.setName(item.getName());
        lessonItemDataRequest.setCode(item.getCode());
        lessonItemDataRequest.setDescription(item.getDescription());
        lessonItemDataRequest.setStatus(status.apiStatus);
        lessonItemDataRequest.setVersion(item.getVersion());
        lessonItemDataRequest.setHomework(item.getHomework());
        lessonItemDataRequest.setCreatedAt(item.getCreatedAt());
        lessonItemDataRequest.setUpdatedAt(item.getUpdatedAt());
        lessonItemDataRequest.setUpdatedBy(item.getUpdatedBy());
        lessonItemDataRequest.setCreatedBy(item.getCreatedBy());

        TopicItemDataRequest topicItemDataRequest = new TopicItemDataRequest();
        topicItemDataRequest.setName("sanity lab");
        topicItemDataRequest.setAssets(item.getTopics().get(0).getTopicAssets());
        topicItemDataRequest.setOrder(1);

        lessonItemDataRequest.setTopics(Collections.singletonList(topicItemDataRequest));
        lessonItemDataRequest.setStatusMode(1);//Manual
        lessonItemDataRequest.setHomeworks(item.getHomeworks());
        lessonItemDataRequest.setIntroduction(new ArrayList<>());
        lessonItemDataRequest.setSummary(new ArrayList<>());
        lessonItemDataRequest.setLessonContent(lessonItemDataLst);
        lessonItemDataRequest.setUuid(lessonId);
        lessonItemDataRequest.setSummaryKeyTakeAway("");

        setModuleToLessonItemDataRequest(moduleName, lessonItemDataRequest);

        practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
    }
    */

    public LessonEditResponse changeLessonStatus(String lessonName, String moduleName, Status status, StatusMode... statusMode) throws ApiException {
        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataResponse item = practiceArenaApi.getSingleLessonData(token, lessonId);
        item.setStatus(status.apiStatus);

        LessonItemDataRequest lessonItemDataRequest = setLessonItemDataRequest(lessonId, moduleName, item, statusMode);

        TopicItemDataRequest topicItemDataRequest = new TopicItemDataRequest();
        setTopicsRequestItem(item, lessonItemDataRequest, topicItemDataRequest);

        LessonEditResponse response = practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
        printResponse(response);
        return response;
    }

    private void setTopicsRequestItem(LessonItemDataResponse item, LessonItemDataRequest lessonItemDataRequest, TopicItemDataRequest topicItemDataRequest) {
        if (item.getTopics().size() > 0) {
            for (TopicItemDataRequest topicItemData : item.getTopics()) {
                topicItemDataRequest.setName(topicItemData.getName());
                topicItemDataRequest.setOrder(topicItemData.getOrder());
                topicItemDataRequest.setAssets(topicItemData.getTopicAssets());
            }
            lessonItemDataRequest.setTopics(Collections.singletonList(topicItemDataRequest));
        } else {
            lessonItemDataRequest.setTopics(Collections.emptyList());
        }
    }

    public ModuleItemData getModuleDataByModuleName(String moduleName) throws ApiException {
        List<ModuleItemData> moduleLst = practiceArenaApi.getPAV2Modules(token);
        return moduleLst.stream().filter(item -> item.getName().equals(moduleName)).findFirst().orElse(null);
    }

    public ModuleListResponse getPAModules() throws ApiException {
        return practiceArenaApi.getPAV2Modules(token);
    }

    public String getModuleIdByModuleName(String moduleName) throws ApiException {
        ArrayList<ModuleItemData> lst = getPAModules();
        return Objects.requireNonNull(lst.stream().filter(moduleItemData -> moduleItemData.getName().equals(moduleName)).findFirst().orElse(null)).getId();
    }

    public LessonListResponse getPALessons() throws ApiException {
        return practiceArenaApi.getPAV2Lessons(token);
    }

    public String getLessonIdByLessonName(String lessonName) throws ApiException {
        ArrayList<LessonItemDataResponse> lst = getPALessons();
        return Objects.requireNonNull(lst.stream().filter(lessonItemData -> lessonItemData.getName().equals(lessonName)).findFirst().orElse(null)).getId();
    }

    public LabListResponse getPALabs() throws ApiException {
        return practiceArenaApi.getPAV2Labs(token);
    }

    public LessonItemDataResponse uploadLesson(String lessonFilePath) throws ApiException {
        practiceArenaApi.getLessonDesc(token, new File(FileUtil.getFile(lessonFilePath)));
        LessonItemDataResponse lessonItemData = practiceArenaApi.installLesson(token, new File(FileUtil.getFile(lessonFilePath)));
        practiceArenaApi.finishLesson(token);
        sleep(Duration.TWO_SECONDS); //added since we want to give the server time to update the new lesson
        return lessonItemData;
    }

    public void createNewModule(String moduleName, UserLevel level, Team team, Status status, StatusMode statusMode, String desc, SkillItems skillPre, SkillItems whatWillSkill) throws ApiException {
        String iconId;
        if (contentManagerAPI.getIconItemDataByName("unicorn.svg") == null) {
            SkillItemDataWithUUID skillItemDataWithUUID = contentManagerAPI.uploadNewIcon(UNICORN_ICON_FILE_PATH, "unicorn.svg");
            iconId = skillItemDataWithUUID.getId();
        } else {
            iconId = contentManagerAPI.getIconItemDataByName("unicorn.svg").getId();
        }

        practiceArenaApi.createNewModule(token, moduleName, level.apiValue, team.getApiTeam(), desc, iconId, status.apiStatus, Collections.emptyList(), Collections.emptyList(),
                skillPre.getValue(), skillPre.getSkillId(), true, "prerequisite",
                whatWillSkill.getValue(), whatWillSkill.getSkillId(), true, "achieved", Collections.emptyList(), statusMode.getValue());
        ModuleItemData moduleItemData = practiceArenaApi.getPAV2Modules(token).stream().filter(item -> item.getName().equals(moduleName)).findFirst().orElse(null);

        Log.i("Module created successfully =[%s]", moduleItemData);
    }

    public void changeVirtualLabStatus(String vlabName, Status status) throws ApiException {
        PaStatus paStatus = new PaStatus();
        paStatus.setStatus(status.apiStatus);
        printBody(paStatus);

        String vlabId = getVirtualIdByVirtualLabName(vlabName);
        StatusResponse paResposne = practiceArenaApi.changeVLabStatus(token, vlabId, paStatus);
        printResponse(paResposne);
    }

    private String getVirtualIdByVirtualLabName(String vlabName) throws ApiException {
        LabListResponse labListResponse = getPALabs();
        return Objects.requireNonNull(labListResponse.stream().filter(item -> item.getName().equals(vlabName)).findFirst().orElse(null)).getId();
    }

    public void deleteModule(String moduleName) throws ApiException {
        String moduleId = getModuleIdByModuleName(moduleName);
        practiceArenaApi.deleteModule(token, moduleId);
    }

    public void deleteLesson(String lessonName) throws ApiException {
        String lessonId = getLessonIdByLessonName(lessonName);
        practiceArenaApi.deleteLesson(token, lessonId);
    }

    private List<Object> setLessonContentData(List<LessonResponseItemData> lessonResponseItemDataList) {
        List<Object> lessonItemLst = new ArrayList<>();

        for (LessonResponseItemData lessonResponseItemData : lessonResponseItemDataList) {
            String contentTypeName = lessonResponseItemData.getContentTypeName();
            String contentId = lessonResponseItemData.getContentId();

            lessonItemLst.add(setLessonItemData(contentId, contentTypeName, contentTypeName));
        }

        return lessonItemLst;
    }

    private Object setLessonItemData(String contentId, String contentTypeName, String name) {
        if(contentId.contains("-")){
            LessonItemDataWithString lessonItemDataWithString = new LessonItemDataWithString();
            lessonItemDataWithString.setContentId(contentId);
            lessonItemDataWithString.setContentTypeName(contentTypeName);
            lessonItemDataWithString.setName(name);
            return lessonItemDataWithString;
        } else {
            LessonItemData lessonItemData = new LessonItemData();
            lessonItemData.setContentId(Integer.valueOf(contentId));
            lessonItemData.setContentTypeName(contentTypeName);
            lessonItemData.setName(name);
            return lessonItemData;
        }
    }

    //Fixme: https://thrivedxlabs.atlassian.net/browse/AUT-482 >>> [SWAGGER] - Create 'PediaItemData' and unify all entity to use it.
    public void assignEntityToLesson(String lessonName, String moduleName, String entityName, List<String> entityNames) throws ApiException {
        CmsContentManagerApi api = new CmsContentManagerApi();

        String lessonId = getLessonIdByLessonName(lessonName);
        LessonItemDataRequest lessonItemDataRequest = getLessonItemDataRequest(lessonId, moduleName, Status.ACTIVE);

        List<Object> lessonContentLst = (List<Object>) lessonItemDataRequest.getLessonContent();

        switch (entityName) {
            case "presentation":
                PresentationsListResponse presentationListResponse = api.cmsContentManagerPresentationsLst(token);
                for (String presentationName : entityNames) {
                    for (PresentationItemData presentationItemData : presentationListResponse) {
                        if (presentationItemData.getName().equalsIgnoreCase(presentationName)) {
                            lessonContentLst.add(setLessonItemData(presentationItemData.getId(), entityName, presentationName));
                            break;
                        }
                    }
                }
                break;
            case "video":
                VideoListResponse videoListResponse = api.cmsContentManagerVideosLst(token);
                for (String videoName : entityNames) {
                    for (VideoItemData videoResponse : videoListResponse) {
                        if (videoResponse.getDisplayName().equalsIgnoreCase(videoName)) {
                            lessonContentLst.add(setLessonItemData(videoResponse.getVideoId(), entityName, videoName));
                            break;
                        }
                    }
                }
                break;
            case "challenge":
                for (String challengeName : entityNames) {
                    String challengeId = challengesAPI.getChallengeIdByName(challengeName);
                    lessonContentLst.add(setLessonItemData(challengeId, "challenge", challengeName));
                }
                break;
        }

        lessonItemDataRequest.setLessonContent(lessonContentLst);

        practiceArenaApi.editLesson(token, lessonId, lessonItemDataRequest);
    }
}
