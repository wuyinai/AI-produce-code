package com.wuyinai.wuaipdce.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;

import com.wuyinai.wuaipdce.ai.AiCodeGenTitleServiceFactory;
import com.wuyinai.wuaipdce.ai.AiCodeGenTypeRoutingService;
import com.wuyinai.wuaipdce.ai.AiCodeGenTypeRoutingServiceFactory;
import com.wuyinai.wuaipdce.common.DeleteRequest;
import com.wuyinai.wuaipdce.constant.AppConstant;
import com.wuyinai.wuaipdce.constant.UserConstant;
import com.wuyinai.wuaipdce.core.AiCodeGeneratorFacade;
import com.wuyinai.wuaipdce.core.builder.VueProjectBuilder;
import com.wuyinai.wuaipdce.core.handler.StreamHandlerExecutor;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.mapper.AppMapper;
import com.wuyinai.wuaipdce.model.dto.app.*;
import com.wuyinai.wuaipdce.model.entity.App;
import com.wuyinai.wuaipdce.model.entity.CollaborationRecord;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.enums.ChatHistoryMessageTypeEnum;
import com.wuyinai.wuaipdce.model.enums.CodeGenTypeEnum;
import com.wuyinai.wuaipdce.model.vo.AppVO;
import com.wuyinai.wuaipdce.model.vo.UserVO;
import com.wuyinai.wuaipdce.service.AppService;
import com.wuyinai.wuaipdce.service.ChatHistoryService;
import com.wuyinai.wuaipdce.service.CollaborationService;
import com.wuyinai.wuaipdce.service.ScreenshotService;
import com.wuyinai.wuaipdce.service.UserService;
import com.wuyinai.wuaipdce.websocket.WebSocketHandler;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
//    @Value("${code.deploy-host:http://localhost}")
//    private String deployHost;
    @Resource
    private UserService userService;
    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;

    @Resource
    private AiCodeGenTitleServiceFactory aiCodeGenTitleServiceFactory ;
    
    @Resource
    private CollaborationService collaborationService;

    @Resource
    @Lazy
    private WebSocketHandler webSocketHandler;

    /**
     * 添加上脱敏用户信息
     *
     * @param app
     * @return
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }


    /**
     * 查询条件封装
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 获取AppVO列表
     *
     * @param appList
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 从appList中提取所有不重复的用户ID（userId），并收集到一个Set集合中，用于后续批量查询用户信息，避免N+1查询问题。
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)//App::getUserId: 方法引用，等价于 app -> app.getUserId()，
                .collect(Collectors.toSet());//收集器，将流中的元素收集到一个 Set 集合中
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()//批量获取用户信息，转换成流
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    /**
     * 添加应用
     *
     * @param appAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public Long addApp(AppAddRequest appAddRequest, User loginUser) {
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        //使用AI智能概括网站标题
        String string = aiCodeGenTitleServiceFactory.aiCodeGenTitleServicePrototype().generateHtmlCode(initPrompt);
        app.setAppName(string);
        // 使用AI智能选择代码生成类型
        AiCodeGenTypeRoutingService routingService = aiCodeGenTypeRoutingServiceFactory.aiCodeGenTypeRoutingServicePrototype();
        CodeGenTypeEnum codeGenTypeEnum = routingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());
        // 设置初始状态为开发中
        app.setStatus("开发中");
        // 插入数据库
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    /**
     * 更新应用
     *
     * @param appUpdateRequest
     * @param request
     */
    @Override
    public void updateApp(AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long id = appUpdateRequest.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人可更新
        if (!oldApp.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        App app = new App();
        app.setId(id);
        app.setAppName(appUpdateRequest.getAppName());
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 删除应用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public boolean deleteApp(DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldApp.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(id);
    }

    /**
     * 根据 id 获取应用详情
     */
    @Override
    public AppVO getAppVOById(Long id) {
        // 查询数据库
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return getAppVO(app);
    }

    /**
     * 分页获取当前用户创建的应用列表
     */
    @Override
    public Page<AppVO> listMyAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询当前用户的应用
        appQueryRequest.setUserId(loginUser.getId());
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 分页获取当前用户的协作应用列表
     */
    @Override
    public Page<AppVO> listCollaborateAppVOByPage(AppQueryRequest appQueryRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        
        // 查询当前用户参与的所有协作记录
        List<CollaborationRecord> collaborationRecords = collaborationService.getCollaborationsByUserId(loginUser.getId());
        if (CollUtil.isEmpty(collaborationRecords)) {
            // 如果没有协作记录，返回空分页
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 筛选出有效的协作记录，并获取关联的应用ID
        List<Long> appIds = collaborationRecords.stream()
                .filter(record -> "valid".equals(record.getStatus()))
                .map(CollaborationRecord::getAppId)
                .distinct()
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(appIds)) {
            // 如果没有关联应用，返回空分页
            return new Page<>(pageNum, pageSize, 0);
        }
        
        // 查询这些应用的信息
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest)
                .in("id", appIds)
                .orderBy("createTime", false);
        
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 分页获取精选应用列表
     */
    @Override
    public Page<AppVO> listSelectedAppVOByPage(AppQueryRequest appQueryRequest) {
        // 限制每页最多 20 个
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多查询 20 个应用");
        long pageNum = appQueryRequest.getPageNum();
        // 只查询精选的应用
        appQueryRequest.setPriority(AppConstant.GOOD_APP_PRIORITY);
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        // 分页查询
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 管理员删除应用
     */
    @Override
    public boolean deleteAppByAdmin(DeleteRequest deleteRequest) {
        long id = deleteRequest.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        return this.removeById(id);
    }

    /**
     * 管理员更新应用
     */
    @Override
    public void updateAppByAdmin(AppAdminUpdateRequest appAdminUpdateRequest) {
        long id = appAdminUpdateRequest.getId();
        // 判断是否存在
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        // 设置编辑时间
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 管理员分页获取应用列表
     */
    @Override
    public Page<AppVO> listAppVOByPageAdmin(AppQueryRequest appQueryRequest) {
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        if (StrUtil.isEmpty(appQueryRequest.getCodeGenType())) {
            appQueryRequest.setCodeGenType(null);
        }
        QueryWrapper queryWrapper = this.getQueryWrapper(appQueryRequest);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        // 数据封装
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        List<AppVO> appVOList = this.getAppVOList(appPage.getRecords());
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 管理员根据 id 获取应用详情
     */
    @Override
    public AppVO getAppVOByIdByAdmin(long id) {
        // 查询数据库
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return this.getAppVO(app);
    }

    /**
     * 调用门面类方法
     *
     * @param appId
     * @param message
     * @param loginUser
     * @return
     */
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        //查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //验证用户是否有权限访问该应用
        if (!app.getUserId().equals(loginUser.getId())) {
            //判断应用是否存在有效协作记录
            CollaborationRecord collaborationRecords = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecords == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
            }
            List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecords.getId());
            if (!userIds.contains(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
            }
        }
        //获取应用的代码类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        //新增：校验代码生成类型
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型不支持");
        }
        //新增：调用对话历史保存方法
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        //发送用户消息给其他协作者
        List<Long> collaborators = this.getCollaborators(appId, loginUser.getId());
        //将用户信息同步给其他用户
        //脱敏后的用户信息
        UserVO userVO = userService.getUserVO(loginUser);
        webSocketHandler.handleCollaborationMessage(message, collaborators,appId,userVO);
        //修改：调用门面方法调用AI生成代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStreaming(message, codeGenTypeEnum, appId);

//        //新增：收集AI响应内容并在完成后记录到对话历史（先创建一个流）
//        StringBuilder contentBuilder = new StringBuilder();
//        return contentFlux
//                .map(chunk -> {
//                    contentBuilder.append(chunk);
//                    return chunk;
//                })
//                .doOnComplete(() -> {
//                    // 流式响应完成后，保存AI消息到对话历史
//                    String aiMessage = contentBuilder.toString();
//                    if (StrUtil.isNotBlank(aiMessage)) {
//                        chatHistoryService.addChatMessage(appId, aiMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                    }
//                })
//                .doOnError(error -> {
//                    // 流式响应出错时，保存错误消息到对话历史
//                    String errorMessage = "AI回复失败" + error.getMessage();
//                    chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageTypeEnum.AI.getValue(), loginUser.getId());
//                });
        return streamHandlerExecutor.doExecute(contentFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum);// 使用流处理器执行器执行流式响应
    }


    /**
     * 应用部署
     *
     * @param appId
     * @param loginUser
     * @return
     */
    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 9. 更新应用的 deployKey、部署时间和状态
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        updateApp.setStatus("发布中");
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 返回可访问的 URL


// 10. 构建应用访问 URL
//        String appDeployUrl = String.format("%s/%s/", deployHost, deployKey);
// 10. 返回可访问的 URL
        String appDeployUrl = String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 异步生成应用截图并更新应用封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl ;
    }

    /**
     * 重写removeById方法，关联删除
     */
    @Override
    public boolean removeById(Serializable id) {
        //校验参数
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id不能为空");
        }
        //转换为Long类型
        Long appId = Long.valueOf(id.toString());
        if (appId <= 0) {
            return false;
        }

        //先删除关联的历史对话
        try {
            chatHistoryService.deleteMessage(appId);
        } catch (Exception e) {
            log.error("删除应用关联的历史对话失败：{}", e.getMessage());
        }

        //删除应用
        return super.removeById(appId);
    }

    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图服务生成截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
            // 更新应用封面字段
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
        });
    }
    
    /**
     * 保存直接修改的内容
     *
     * @param appSaveDirectEditRequest 保存请求
     * @param loginUser 登录用户
     */
    @Override
    public void saveDirectEdit(AppSaveDirectEditRequest appSaveDirectEditRequest, User loginUser) {
        Long appId = appSaveDirectEditRequest.getAppId();
        List<AppSaveDirectEditRequest.EditedFile> files = appSaveDirectEditRequest.getFiles();
        
        // 1. 查询应用
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        // 2. 验证用户是否有权限访问该应用
        if (!app.getUserId().equals(loginUser.getId())) {
            //判断应用是否存在有效协作记录
            CollaborationRecord collaborationRecords = collaborationService.getCollaborationRecordByAppId(appId);
            if (collaborationRecords != null) {
                List<Long> userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecords.getId());
                if (!userIds.contains(loginUser.getId())){
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }
        }
        
        // 3. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        
        // 4. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(), ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        
        // 5. 遍历修改的文件列表，保存文件内容
        for (AppSaveDirectEditRequest.EditedFile editedFile : files) {
            String filePath = editedFile.getFilePath();
            String content = editedFile.getContent();
            
            // 对于HTML类型的应用，直接保存为index.html
            // 对于其他类型的应用，根据实际情况处理
            String finalFilePath = "index.html";
            
            // 构建完整的文件路径，直接修改源文件
            File file = new File(sourceDir, finalFilePath);
            
            // 确保文件所在目录存在
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 保存文件内容
            FileUtil.writeString(content, file, "UTF-8");
            log.info("直接修改保存文件: {}", file.getAbsolutePath());
        }
        
        // 6. 更新应用的编辑时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setEditTime(LocalDateTime.now());
        boolean updated = this.updateById(updateApp);
        ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用编辑时间失败");
    }

    /**
     * 查询当前应用的协作者，并排除自己
     */
    @Override
    public List<Long> getCollaborators(Long appId, Long userId) {
        //根据应用ID查询出所有的用户信息，在去除当前登录用户。
        //先判断当前应用是否有有效的协作记录
        CollaborationRecord collaborationRecordByAppId = collaborationService.getCollaborationRecordByAppId(appId);
        List<Long> userIds;
        //TODO 待优化，检测当前协作者是否在线。
        if ( collaborationRecordByAppId != null){
            //获取当前协作记录的所有用户id
            userIds = collaborationService.getCollaboratorsByCollaborationId(collaborationRecordByAppId.getId());
            userIds.remove(userId);
        } else {
            userIds = new ArrayList<>();
        }
        return userIds;
    }

    /**
     * 获取应用源码
     */
    @Override
    public String getAppSourceCode(Long appId, User loginUser) {
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        }
        List<File> htmlFiles = FileUtil.loopFiles(sourceDir, file -> file.getName().endsWith(".html"));
        if (htmlFiles.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到HTML文件");
        }
        File mainHtmlFile = htmlFiles.get(0);
        return FileUtil.readUtf8String(mainHtmlFile);
    }

    /**
     * 获取应用源码目录结构
     */
    @Override
    public List<SourceCodeFileDTO> getAppSourceDir(Long appId, User loginUser) {
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        }
        return buildFileTree(sourceDir, sourceDir);
    }

    /**
     * 递归构建文件树
     */
    private List<SourceCodeFileDTO> buildFileTree(File dir, File rootDir) {
        List<SourceCodeFileDTO> result = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return result;
        }
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            }
            if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            }
            return f1.getName().compareTo(f2.getName());
        });
        for (File file : files) {
            SourceCodeFileDTO dto = new SourceCodeFileDTO();
            dto.setName(file.getName());
            String relativePath = file.getAbsolutePath().substring(rootDir.getAbsolutePath().length());
            dto.setPath(relativePath.replace("\\", "/"));
            dto.setIsDir(file.isDirectory());
            if (file.isDirectory()) {
                dto.setChildren(buildFileTree(file, rootDir));
                dto.setExt(null);
            } else {
                dto.setExt(getFileExtension(file.getName()));
                dto.setSize(file.length());
            }
            result.add(dto);
        }
        return result;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 获取应用指定文件源码
     */
    @Override
    public String getAppSourceFile(Long appId, String filePath, User loginUser) {
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        }
        String normalizedPath = filePath.replace("/", File.separator);
        File targetFile = new File(sourceDir, normalizedPath);
        if (!targetFile.exists() || !targetFile.isFile()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在: " + filePath);
        }
        return FileUtil.readUtf8String(targetFile);
    }

    /**
     * 保存应用指定文件源码
     */
    @Override
    public void saveSourceFile(Long appId, String filePath, String content, User loginUser) {
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用代码不存在");
        }
        String normalizedPath = filePath.replace("/", File.separator);
        File targetFile = new File(sourceDir, normalizedPath);
        if (!targetFile.exists() || !targetFile.isFile()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "文件不存在: " + filePath);
        }
        FileUtil.writeUtf8String(content, targetFile);
    }
}
