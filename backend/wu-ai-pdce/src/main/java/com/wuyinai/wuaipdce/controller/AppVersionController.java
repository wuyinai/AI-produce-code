package com.wuyinai.wuaipdce.controller;

import com.wuyinai.wuaipdce.common.BaseResponse;
import com.wuyinai.wuaipdce.common.ResultUtils;
import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionCreateRequest;
import com.wuyinai.wuaipdce.model.dto.appversion.AppVersionVO;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.service.AppVersionService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/version")
public class AppVersionController {

    @Resource
    private AppVersionService appVersionService;

    @Resource
    private UserService userService;

    @PostMapping("/create")
    public BaseResponse<Long> createVersion(@RequestBody AppVersionCreateRequest request,
                                             HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Long versionId = appVersionService.createVersion(request, loginUser);
        return ResultUtils.success(versionId);
    }

    @GetMapping("/list/{appId}")
    public BaseResponse<List<AppVersionVO>> listVersions(@PathVariable Long appId,
                                                          HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        List<AppVersionVO> versions = appVersionService.listVersionsByAppId(appId, loginUser);
        return ResultUtils.success(versions);
    }

    @GetMapping("/current/{appId}")
    public BaseResponse<AppVersionVO> getCurrentVersion(@PathVariable Long appId,
                                                        HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        AppVersionVO version = appVersionService.getCurrentVersion(appId, loginUser);
        return ResultUtils.success(version);
    }

    @PostMapping("/rollback/{versionId}")
    public BaseResponse<Boolean> rollbackToVersion(@PathVariable Long versionId,
                                                    HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = appVersionService.rollbackToVersion(versionId, loginUser);
        return ResultUtils.success(result);
    }

    @DeleteMapping("/delete/{versionId}")
    public BaseResponse<Boolean> deleteVersion(@PathVariable Long versionId,
                                                HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = appVersionService.deleteVersion(versionId, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/snapshot/{versionId}")
    public BaseResponse<String> getVersionSnapshot(@PathVariable Long versionId,
                                                    HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        String snapshot = appVersionService.getVersionCodeSnapshot(versionId, loginUser);
        return ResultUtils.success(snapshot);
    }
}
