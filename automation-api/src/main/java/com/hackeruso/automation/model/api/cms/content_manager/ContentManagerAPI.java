package com.hackeruso.automation.model.api.cms.content_manager;

import com.hackeruso.automation.conf.EnvConf;
import com.hackeruso.automation.model.api.BaseAPI;
import com.hackeruso.automation.utils.FileUtil;
import io.swagger.client.ApiException;
import io.swagger.client.api.CmsContentManagerApi;
import io.swagger.client.model.*;

import java.io.File;
import java.util.ArrayList;

import static com.hackeruso.automation.logger.LoggerFactory.Log;

public class ContentManagerAPI extends BaseAPI {
    private final CmsContentManagerApi cmsContentManagerApi = new CmsContentManagerApi();
    private final String PPT_FILE_PATH = EnvConf.getProperty("automation.ppt.file.payload");
    private final String TED_JPG_IMAGE_FILE_PATH = EnvConf.getProperty("automation.ted.small.size.file.path.location");
    private final String VIDEO_FILE_PATH = EnvConf.getProperty("automation.video.file.payload");

    public ContentManagerAPI(String host, String token) {
        super(host, token);
        cmsContentManagerApi.setApiClient(restApi.getClient());
    }

    public void deleteIcon(String iconName) throws ApiException {
        IconRequestId iconRequestId = new IconRequestId();
        iconRequestId.setId(getIconItemDataByName(iconName).getId());
        printBody(iconRequestId);
        cmsContentManagerApi.deleteIcon(token, iconRequestId);
    }

    public VideoListResponse getVideosList() throws ApiException {
        return cmsContentManagerApi.cmsContentManagerVideosLst(token);
    }

    public PresentationsListResponse getPresentationsList() throws ApiException {
        return cmsContentManagerApi.cmsContentManagerPresentationsLst(token);
    }

    public SkillListResponse getSkillList() throws ApiException {
        return cmsContentManagerApi.cmsContentMangerSkillLst(token, 1000, 1);
    }

    public PresentationItemData getPresentationItemByIndex(int index) throws ApiException {
        PresentationItemData item = getPresentationsList().get(index);
        writeRequestParamsToLog(item);
        return item;
    }

    public PresentationItemData uploadPresentation(String presentationName, String description) throws ApiException {
        PresentationItemData itemData = cmsContentManagerApi.uploadPresentation(token,
                new File(FileUtil.getFile(TED_JPG_IMAGE_FILE_PATH)),
                new File(FileUtil.getFile(PPT_FILE_PATH)), presentationName, description, "0");
        printResponse(itemData);
        Log.info("Uploading presentation passed successfully");
        return itemData;
    }

    public CreateVideoResponse uploadNewVideo(String videoName, String description) throws ApiException {
        CreateVideoResponse createVideoResponse = cmsContentManagerApi.uploadNewVideo(token,
                new File(FileUtil.getFile(TED_JPG_IMAGE_FILE_PATH)),
                new File(FileUtil.getFile(VIDEO_FILE_PATH)), 0, videoName, description);
        printResponse(createVideoResponse);
        Log.info("Uploading video passed successfully");
        return createVideoResponse;
    }

    public SkillItemDataWithUUID uploadNewIcon(String iconFile, String fileName) throws ApiException {
        if (getIconItemDataByName(fileName) == null) {
            cmsContentManagerApi.uploadIcon(token, new File(FileUtil.getFile(iconFile)));
            IconRequestBody body = new IconRequestBody();
            body.addFilesNameItem(fileName);
            printBody(body);
            return cmsContentManagerApi.saveIcon(token, body).get(0);
        }
        return null;
    }

    public ArrayList<SkillItemDataWithUUID> getIconList() throws ApiException {
        return cmsContentManagerApi.getIconList(token);
    }

    public SkillItemDataWithUUID getIconItemDataByName(String iconFileName) throws ApiException {
        ArrayList<SkillItemDataWithUUID> lst;
        try {
            lst = getIconList();
            return lst.stream().filter(skillItemDataWithUUID -> skillItemDataWithUUID.getName().equals(iconFileName)).findFirst().orElse(null);
        } catch (NullPointerException e) {
            Log.e(e.getMessage());
            return null;
        }
    }

}
