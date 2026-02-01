package com.wuyinai.wuaipdce.service;

import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionCreateRequest;
import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionVO;
import com.wuyinai.wuaipdce.model.entity.AppVersion;
import com.wuyinai.wuaipdce.model.entity.User;


import java.util.List;

public interface AppVersionService {

    Long createVersion(AppVersionCreateRequest request, User loginUser);

    List<AppVersionVO> listVersionsByAppId(Long appId, User loginUser);

    AppVersionVO getCurrentVersion(Long appId, User loginUser);

    boolean rollbackToVersion(Long versionId, User loginUser);

    boolean deleteVersion(Long versionId, User loginUser);

    String getVersionCodeSnapshot(Long versionId, User loginUser);

    void autoCreateVersion(Long appId, String versionDescription, String triggerMessage, Long userId);
}
