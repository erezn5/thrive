package com.hackeruso.automation.model.api.cms.syllabi;

import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.model.api.cms.categories.ChallengeCategoriesAPI;
import com.hackeruso.automation.model.api.cms.challenges.ChallengesAPI;
import com.hackeruso.automation.model.api.cms.cyberpedia.CyberpediaAPI;
import com.hackeruso.automation.model.api.cms.institutions.ClassesAPI;
import com.hackeruso.automation.model.api.cms.institutions.CollegesAPI;
import com.hackeruso.automation.model.api.cms.institutions.InstitutionsAPI;
import com.hackeruso.automation.model.api.cms.practice_arena.PracticeArenaAPI;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsSyllabiApi;
import io.swagger.client.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class SyllabusAPI extends BaseAPI {

    private final CmsSyllabiApi cmsSyllabiApi = new CmsSyllabiApi();
    private final InstitutionsAPI institutionsAPI;
    private final ClassesAPI classesAPI;
    private final CollegesAPI collegesAPI;
    private final ChallengeCategoriesAPI challengeCategoriesAPI;
    private final ChallengesAPI challengesAPI;
    private final CyberpediaAPI cyberpediaAPI;
    private final PracticeArenaAPI practiceArenaAPI;


    public SyllabusAPI(String host, String token) {
        super(host, token);
        cmsSyllabiApi.setApiClient(restApi.getClient());
        institutionsAPI = new InstitutionsAPI(host, token);
        collegesAPI = new CollegesAPI(host, token);
        classesAPI = new ClassesAPI(host, token);
        challengeCategoriesAPI = new ChallengeCategoriesAPI(host, token);
        challengesAPI = new ChallengesAPI(host, token);
        cyberpediaAPI = new CyberpediaAPI(host, token);
        practiceArenaAPI = new PracticeArenaAPI(host, token);
    }

    public void createSyllabusWithContent(String institutionName, String syllabusName, List<String> challengeNames, List<String> cyberpediaList, List<String> moduleLst, String lessonName) throws ApiException {

        CreateSyllabusBodyPayload body = new CreateSyllabusBodyPayload();
        body.setName(syllabusName);
        body.setInstituteName(institutionName);
        int institutionId = institutionsAPI.getInstitutionIdByName(institutionName);
        body.setInstituteId(String.valueOf(institutionId));

        List<ChallengeCategoryItem> challengeCategoryLst = setChallengeCategoriesInSyllabus(challengeNames);
        body.setChallengesCategories(challengeCategoryLst);
        Log.i("Attaching challenge categories to syllabus=[%s] is successful", syllabusName);

        List<CyberpediaItem> cyberpediaItemLst = setCyberpediaInSyllabus(cyberpediaList);
        body.setCyberpedia(cyberpediaItemLst);
        Log.i("Attaching cyberpedia categories to syllabus=[%s] is successful", syllabusName);

        List<Pav2Item> pav2ItemList;
        if (moduleLst.isEmpty()) {
            pav2ItemList = new ArrayList<>();
        } else {
            pav2ItemList = setModulesInSyllabus(moduleLst, lessonName);
        }
        body.setPav2Modules(pav2ItemList);
        Log.i("Attaching modules to syllabus=[%s] is successful", syllabusName);

        cmsSyllabiApi.createNewSyllabus(token, body);
        Log.i("New Syllabus [%s] created with:", syllabusName);
        challengeCategoryLst.forEach(item -> Log.i("Challenge Category [%s]", item.getName()));
        cyberpediaItemLst.forEach(item -> Log.i("Cyberpedia [%s]", item.getName()));
        pav2ItemList.forEach(item -> Log.i("Module [%s]", item.getName()));
    }

    private List<ChallengeCategoryItem> setChallengeCategoriesInSyllabus(List<String> challengeNames) throws ApiException {
        List<ChallengeCategoryItem> challengeCategoryLst = new ArrayList<>();//inside body
        List<ChallengeItem> challengeItems = new ArrayList<>();//inside challengeCategoryItem

        for (String challengeNameItem : challengeNames) {
            int challengeCategoryIndex = 0;
            DataWithUUID challengeInfo = challengesAPI.getChallengeItemInfoByName(challengeNameItem);
            ChallengeDetailedItem challengeDetailedItem = challengesAPI.getSpecificDetailedChallengeData(challengeNameItem);
            DataWithUUID challengeCategoryInfo = challengeCategoriesAPI.getCategoryItemByName(challengeDetailedItem.getCategoryName());
            ChallengeCategoryItem challengeCategoryItem = new ChallengeCategoryItem();

            //region - "challenges category"
            challengeCategoryItem.setId(challengeDetailedItem.getCategoryId());
            challengeCategoryItem.setName(challengeDetailedItem.getCategoryName());
            challengeCategoryItem.setDescription(challengeCategoryInfo.getDescription());
            challengeCategoryItem.setSlug(challengeDetailedItem.getCategorySlug());
            challengeCategoryItem.setImage(challengeDetailedItem.getCategoryImage());
            challengeCategoryItem.setStatus(challengeDetailedItem.getCategoryStatus());
            challengeCategoryItem.setOrderby(null);
            challengeCategoryItem.setCreatedAt(challengeCategoryInfo.getCreatedAt());
            challengeCategoryItem.setUpdatedAt(challengeCategoryInfo.getUpdatedAt());
            challengeCategoryItem.setStatusMode(challengeCategoryInfo.getStatusMode());
            challengeCategoryItem.setS3Image(challengeCategoryInfo.getS3Image());
            challengeCategoryItem.setTotal(2);
            challengeCategoryItem.setPicked(true);
            //endregion

            //region - "challenges"
            ChallengeItem challengeItem = new ChallengeItem();
            challengeItem.setHintPoints(challengeDetailedItem.getHintPoints());
            challengeItem.setId(challengeDetailedItem.getId());
            challengeItem.setChallengeStatus(challengeDetailedItem.getStatus());
            challengeItem.setVersion(challengeDetailedItem.getVersion());
            challengeItem.setCategoryId(challengeDetailedItem.getCategoryId());
            challengeItem.setName(challengeDetailedItem.getName());
            challengeItem.setStory(challengeDetailedItem.getStory());
            challengeItem.setStatus(challengeInfo.getStatus());
            challengeItem.setTeamId(challengeDetailedItem.getTeamId());
            challengeItem.setLocked(challengeDetailedItem.getLocked());
            challengeItem.setPoints(challengeDetailedItem.getPoints());
            challengeItem.setPointsName(challengeDetailedItem.getPointsName());
            challengeItem.setHint(challengeDetailedItem.getHint());
            challengeItem.setImage(challengeDetailedItem.getImage());
            challengeItem.setS3Image(challengeInfo.getS3Image());
            challengeItem.setPicked(true);
            //endregion
            challengeItems.add(challengeCategoryIndex, challengeItem);

            challengeCategoryItem.setChallenges(challengeItems);

            challengeCategoryLst.add(challengeCategoryIndex, challengeCategoryItem);
        }
        return challengeCategoryLst;
    }

    private List<Pav2Item> setModulesInSyllabus(List<String> moduleList, String lessonName) throws ApiException {
        String lessonId = practiceArenaAPI.getLessonIdByLessonName(lessonName);
        LessonItemDataResponse lessonItemDataResponse = practiceArenaAPI.getSpecificLessonDataByName(lessonName);

        List<Pav2Item> pav2Items = new ArrayList<>();//inside body
        List<LessonItem> lessonItemList = new ArrayList<>();//inside pav2Items
        List<InstitutionItem> institutionItemList = new ArrayList<>(); //inside pav2Items

        int pav2Counter = 0;
        for (String moduleName : moduleList) {
            Pav2Item pav2Item = new Pav2Item();
            LessonItem lessonItem = new LessonItem();
            InstitutionItem institutionItem = new InstitutionItem();

            ModuleItemData moduleItemData = practiceArenaAPI.getModuleDataByModuleName(moduleName);
            String moduleId = moduleItemData.getId();
            int syllabusId = moduleItemData.getSyllabusId();

            pav2Item.setName(moduleName);
            pav2Item.setId(moduleId);
            pav2Item.setSyllabusId(syllabusId);
            pav2Item.setPicked(true);
            //region "lessons"
            lessonItem.setSyllabusId(syllabusId);
            lessonItem.setContentTypeId(15);
            lessonItem.setContentId(lessonId);
            lessonItem.setOrder(1);
            lessonItem.setCreatedAt(lessonItemDataResponse.getCreatedAt());
            lessonItem.setCreatedBy(Integer.parseInt(lessonItemDataResponse.getCreatedBy()));
            //endregion
            lessonItemList.add(pav2Counter, lessonItem);
            pav2Item.setLessons(lessonItemList);
            //region "institution"
            institutionItem.setInstitution(null);
            institutionItem.setInstitutionId(null);
            //endregion
            institutionItemList.add(pav2Counter, institutionItem);
            pav2Item.setInstitutions(institutionItemList);
            pav2Items.add(pav2Counter, pav2Item);
        }
        return pav2Items;
    }

    private List<CyberpediaItem> setCyberpediaInSyllabus(List<String> cyberpediaList) throws ApiException {
        List<CyberpediaItem> cyberpediaItemLst = new ArrayList<>();
        int pediaCounter = 0;
        for (String cyberpediaName : cyberpediaList) {
            CyberpediaItem cyberpediaItem = new CyberpediaItem();
            DataWithUUID dataWithUUID = cyberpediaAPI.getCyberpediaCategoryDataByName(cyberpediaName);
            String cyberpediaCategoryId = dataWithUUID.getId();
            String cyberpediaCategoryName = dataWithUUID.getName();
            String cyberpediaCategoryDesc = dataWithUUID.getDescription();
            String cyberpediaCategoryImg = dataWithUUID.getImage();
            int cyberpediaCategoryStatus = dataWithUUID.getStatus();
            String cyberpediaCategoryCreatedAt = dataWithUUID.getCreatedAt();
            String cyberpediaCategoryUpdatedAt = dataWithUUID.getUpdatedAt();
            int cyberpediaCategoryCreatedBy = dataWithUUID.getCreatedBy();
            int cyberpediaCategoryStatusMode = dataWithUUID.getStatusMode();

            cyberpediaItem.setId(cyberpediaCategoryId);
            cyberpediaItem.setName(cyberpediaCategoryName);
            cyberpediaItem.setDescription(cyberpediaCategoryDesc);
            cyberpediaItem.setImage(cyberpediaCategoryImg);
            cyberpediaItem.setStatus(cyberpediaCategoryStatus);
            cyberpediaItem.setCreatedAt(cyberpediaCategoryCreatedAt);
            cyberpediaItem.setUpdatedAt(cyberpediaCategoryUpdatedAt);
            cyberpediaItem.setCreatedBy(cyberpediaCategoryCreatedBy);
            cyberpediaItem.setUpdatedBy(null);
            cyberpediaItem.setStatusMode(cyberpediaCategoryStatusMode);
            cyberpediaItem.setPicked(true);
            cyberpediaItemLst.add(pediaCounter, cyberpediaItem);
            pediaCounter++;
        }
        return cyberpediaItemLst;
    }

    public void createSyllabus(String institutionName, String syllabusName) throws ApiException {
        List<SyllabiDescription> syllabiDescriptions = getSyllabusLstByInstitutionName(institutionName);

        if (syllabiDescriptions.stream().anyMatch(syllabi -> syllabi.getName().equalsIgnoreCase(syllabusName))) {
            return;
        }

        CreateSyllabusBodyPayload body = new CreateSyllabusBodyPayload();
        body.setName(syllabusName);
        body.setInstituteName(institutionName);
        int institutionId = institutionsAPI.getInstitutionIdByName(institutionName);
        body.setInstituteId(String.valueOf(institutionId));
        body.setChallengesCategories(null);
        body.setPav2Modules(null);
        body.setCyberpedia(null);
        cmsSyllabiApi.createNewSyllabus(token, body);
    }

    /**
     * @param institutionName - the institution which all syllabi attached to it
     * @return list of syllabi or return null if no institution is attached to any syllabus
     */
    public List<SyllabiDescription> getSyllabusLstByInstitutionName(String institutionName) throws ApiException {
        SyllabiListResponse res = institutionsAPI.getListOfSyllabi();
        for (SyllabusItemList re : res) {
            if (re.getInstitution().equals(institutionName)) {
                return re.getSyllabi();
            }
        }

        return null;
    }

    public void attachSyllabusToClass(String institutionName, String syllabusName, String className) throws ApiException {
        attachSyllabusToEntity(institutionName, syllabusName, className, "class");
    }

    public void attachSyllabusToCollege(String institutionName, String syllabusName, String collegeName) throws ApiException {
        attachSyllabusToEntity(institutionName, syllabusName, collegeName, "college");
    }

    private void attachSyllabusToEntity(String institutionName, String syllabusName, String entityName, String type) throws ApiException {
        int entityId = type.equalsIgnoreCase("college") ? collegesAPI.getCollegeIdByName(entityName) : classesAPI.getClassIdByName(entityName);

        SyllabiAttachmentPayload body = new SyllabiAttachmentPayload();
        SyllabiItemData syllabiItemData = new SyllabiItemData();
        syllabiItemData.setName(syllabusName);
        syllabiItemData.setDefault(true);
        syllabiItemData.setPicked(true);
        List<SyllabiDescription> listOfSyllabuses = getSyllabusLstByInstitutionName(institutionName);

        attachSyllabusByAPI(syllabusName, entityName, type, entityId, body, syllabiItemData, listOfSyllabuses);
    }

    private void attachSyllabusByAPI(String syllabusName, String entityName, String type, int entityId, SyllabiAttachmentPayload body, SyllabiItemData syllabiItemData, List<SyllabiDescription> listOfSyllabuses) throws ApiException {
        for (SyllabiDescription syllabus : listOfSyllabuses) {
            if (syllabus.getName().equalsIgnoreCase(syllabusName)) {
                syllabiItemData.setId(syllabus.getId());
                body.setSyllabi(Collections.singletonList(syllabiItemData));
                body.setEntity(type);
                body.setEntityId(entityId);

                if (entityName.equalsIgnoreCase("college")) {
                    cmsSyllabiApi.cmsCollegeAttachSyllabus(token, body);
                } else {
                    cmsSyllabiApi.cmsClassAttachSyllabus(token, body);
                }
                Log.i("Syllabus=[%s] is attached to entity=[%s]", syllabusName, entityName);
                return;
            }
        }
    }
}
