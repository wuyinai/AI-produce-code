package com.wuyinai.wuaipdce.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wuyinai.wuaipdce.constant.AppConstant;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.mapper.AppMapper;
import com.wuyinai.wuaipdce.mapper.AppVersionMapper;
import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionCreateRequest;
import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionVO;
import com.wuyinai.wuaipdce.model.entity.App;
import com.wuyinai.wuaipdce.model.entity.AppVersion;
import com.wuyinai.wuaipdce.model.entity.CollaborationRecord;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.UserVO;
import com.wuyinai.wuaipdce.service.AppService;
import com.wuyinai.wuaipdce.service.AppVersionService;
import com.wuyinai.wuaipdce.service.CollaborationService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private CollaborationService collaborationService;

    @Resource
    private AppMapper appMapper;
    
    @Resource
    private AppVersionMapper appVersionMapper;
    

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVersion(AppVersionCreateRequest request, User loginUser) {
        Long appId = request.getAppId();
        String triggerType = request.getTriggerType();
        String triggerMessage = request.getTriggerMessage();
        String versionDescription = request.getVersionDescription();

        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(triggerType), ErrorCode.PARAMS_ERROR, "触发类型不能为空");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecord != null) {
                List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecord.getId());
                if (!userIds.contains(loginUser.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
                }
            } else {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
        }

        Integer nextVersionNumber =  appVersionMapper.getNextVersionNumber(appId);
        String versionName = "v" + nextVersionNumber + ".0";

        String codeSnapshot = captureCodeSnapshot(app);

        appVersionMapper.setAllVersionsNotCurrent(appId);

        AppVersion appVersion = AppVersion.builder()
                .appId(appId)
                .versionNumber(nextVersionNumber)
                .versionName(versionName)
                .versionDescription(versionDescription)
                .codeSnapshot(codeSnapshot)
                .triggerType(triggerType)
                .triggerMessage(triggerMessage)
                .isCurrent(1)
                .createUserId(loginUser.getId())
                .build();

        boolean result = this.save(appVersion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建版本失败");

        log.info("应用 {} 创建版本 {} 成功，触发类型: {}", appId, versionName, triggerType);
        return appVersion.getId();
    }

    @Override
    public List<AppVersionVO> listVersionsByAppId(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecord != null) {
                List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecord.getId());
                if (!userIds.contains(loginUser.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
                }
            } else {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
        }

        List<AppVersion> versions = appVersionMapper.listVersionsByAppId(appId);
        if (CollUtil.isEmpty(versions)) {
            return new ArrayList<>();
        }

        List<Long> userIds = versions.stream()
                .map(AppVersion::getCreateUserId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));

        return versions.stream().map(version -> {
            AppVersionVO vo = new AppVersionVO();
            BeanUtil.copyProperties(version, vo);
            UserVO userVO = userVOMap.get(version.getCreateUserId());
            if (userVO != null) {
                vo.setCreateUserName(userVO.getUserName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public AppVersionVO getCurrentVersion(Long appId, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");

        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecord != null) {
                List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecord.getId());
                if (!userIds.contains(loginUser.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
                }
            } else {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
        }

        AppVersion version = appVersionMapper.getCurrentVersionByAppId(appId);
        if (version == null) {
            return null;
        }

        AppVersionVO vo = new AppVersionVO();
        BeanUtil.copyProperties(version, vo);
        User user = userService.getById(version.getCreateUserId());
        if (user != null) {
            vo.setCreateUserName(user.getUserName());
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackToVersion(Long versionId, User loginUser) {
        ThrowUtils.throwIf(versionId == null || versionId <= 0, ErrorCode.PARAMS_ERROR, "版本ID不能为空");

        AppVersion targetVersion = this.getById(versionId);
        ThrowUtils.throwIf(targetVersion == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");

        Long appId = targetVersion.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有应用创建者可以回退版本");
        }

        if (targetVersion.getIsCurrent() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "当前已经是最新版本");
        }

        restoreCodeSnapshot(app, targetVersion.getCodeSnapshot());

        appVersionMapper.setAllVersionsNotCurrent(appId);

        targetVersion.setIsCurrent(1);
        boolean result = this.updateById(targetVersion);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "回退版本失败");

        log.info("应用 {} 回退到版本 {} 成功", appId, targetVersion.getVersionName());
        return true;
    }

    @Override
    public boolean deleteVersion(Long versionId, User loginUser) {
        ThrowUtils.throwIf(versionId == null || versionId <= 0, ErrorCode.PARAMS_ERROR, "版本ID不能为空");

        AppVersion version = this.getById(versionId);
        ThrowUtils.throwIf(version == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");

        Long appId = version.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限删除版本");
        }

        if (version.getIsCurrent() == 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能删除当前版本");
        }

        return this.removeById(versionId);
    }

    @Override
    public String getVersionCodeSnapshot(Long versionId, User loginUser) {
        ThrowUtils.throwIf(versionId == null || versionId <= 0, ErrorCode.PARAMS_ERROR, "版本ID不能为空");

        AppVersion version = this.getById(versionId);
        ThrowUtils.throwIf(version == null, ErrorCode.NOT_FOUND_ERROR, "版本不存在");

        Long appId = version.getAppId();
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");

        if (!app.getUserId().equals(loginUser.getId())) {
            CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecord != null) {
                List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecord.getId());
                if (!userIds.contains(loginUser.getId())) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
                }
            } else {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作");
            }
        }

        return version.getCodeSnapshot();
    }

    private String captureCodeSnapshot(App app) {
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + app.getId();
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            log.warn("应用代码目录不存在: {}", sourceDirPath);
            return "{}";
        }

        List<File> files = FileUtil.loopFiles(sourceDir);
        Map<String, Object> snapshot = new HashMap<>();
        List<Map<String, Object>> fileList = new ArrayList<>();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }

            String relativePath = file.getAbsolutePath().substring(sourceDir.getAbsolutePath().length());
            relativePath = relativePath.replace("\\", "/");

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("path", relativePath);
            fileInfo.put("content", FileUtil.readUtf8String(file));
            fileInfo.put("size", file.length());
            fileList.add(fileInfo);
        }

        snapshot.put("files", fileList);
        snapshot.put("metadata", Map.of(
                "totalFiles", fileList.size(),
                "totalSize", fileList.stream().mapToLong(f -> (Long) f.get("size")).sum(),
                "codeGenType", codeGenType
        ));

        return JSONUtil.toJsonStr(snapshot);
    }

    private void restoreCodeSnapshot(App app, String codeSnapshot) {
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + app.getId();
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;

        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists()) {
            sourceDir.mkdirs();
        } else {
            FileUtil.del(sourceDir);
            sourceDir.mkdirs();
        }

        if (StrUtil.isBlank(codeSnapshot) || "{}".equals(codeSnapshot)) {
            log.warn("代码快照为空，跳过恢复");
            return;
        }

        Map<String, Object> snapshot = JSONUtil.toBean(codeSnapshot, Map.class);
        List<Map<String, Object>> files = (List<Map<String, Object>>) snapshot.get("files");

        if (CollUtil.isEmpty(files)) {
            log.warn("代码快照中没有文件，跳过恢复");
            return;
        }

        for (Map<String, Object> fileInfo : files) {
            String filePath = (String) fileInfo.get("path");
            String content = (String) fileInfo.get("content");

            File file = new File(sourceDir, filePath);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            FileUtil.writeUtf8String(content, file);
        }

        log.info("应用 {} 代码恢复成功，共恢复 {} 个文件", app.getId(), files.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCreateVersion(Long appId, String versionDescription, String triggerMessage, Long userId) {
        try {
            App app = appService.getById(appId);
            if (app == null) {
                log.warn("应用不存在，跳过创建版本: appId={}", appId);
                return;
            }

            Integer nextVersionNumber = appVersionMapper.getNextVersionNumber(appId);
            String versionName = "v" + nextVersionNumber + ".0";

            String codeSnapshot = captureCodeSnapshot(app);

            appVersionMapper.setAllVersionsNotCurrent(appId);

            AppVersion appVersion = AppVersion.builder()
                    .appId(appId)
                    .versionNumber(nextVersionNumber)
                    .versionName(versionName)
                    .versionDescription(versionDescription)
                    .codeSnapshot(codeSnapshot)
                    .triggerType("ai_chat")
                    .triggerMessage(triggerMessage)
                    .isCurrent(1)
                    .createUserId(userId)
                    .build();

            boolean result = this.save(appVersion);
            if (result) {
                log.info("自动创建应用 {} 版本 {} 成功", appId, versionName);
            } else {
                log.warn("自动创建应用 {} 版本 {} 失败", appId, versionName);
            }
        } catch (Exception e) {
            log.error("自动创建版本失败: appId={}, error={}", appId, e.getMessage(), e);
            throw e;
        }
    }
}
