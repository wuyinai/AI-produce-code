package com.wuyinai.wuaipdce.controller;

import com.wuyinai.wuaipdce.common.BaseResponse;
import com.wuyinai.wuaipdce.common.ResultUtils;
import com.wuyinai.wuaipdce.model.dto.collaboration.CollaborationRequest;
import com.wuyinai.wuaipdce.model.entity.CollaborationRecord;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.CollaborationMemberVO;
import com.wuyinai.wuaipdce.service.CollaborationService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 协作 控制层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@RestController
@RequestMapping("/collaboration")
public class CollaborationController {

    @Resource
    private CollaborationService collaborationService;

    @Resource
    private UserService userService;

    /**
     * 开始协作
     * @param appId 应用ID
     * @param request 请求对象
     * @return 协作记录ID
     */
    @PostMapping("/start/{appId}")
    public BaseResponse<Long> startCollaboration(@PathVariable Long appId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long collaborationId = collaborationService.startCollaboration(appId, loginUser.getId());
        return ResultUtils.success(collaborationId);
    }

    /**
     * 添加协作者
     * @param collaborationId 协作记录ID
     * @param request 协作请求，包含要添加的用户ID
     * @return 成功响应
     */
    @PostMapping("/add/{collaborationId}")
    public BaseResponse<Boolean> addCollaborator(@PathVariable Long collaborationId, @RequestBody CollaborationRequest request) {
        collaborationService.addCollaborator(collaborationId, request.getUserId());
        return ResultUtils.success(true);
    }

    /**
     * 退出协作
     * @param collaborationId 协作记录ID
     * @return 成功响应
     */
    @PostMapping("/exit/{collaborationId}")
    public BaseResponse<Boolean> exitCollaboration(@PathVariable Long collaborationId) {
        collaborationService.exitCollaboration(collaborationId);
        return ResultUtils.success(true);
    }

    /**
     * 获取协作记录
     * @param collaborationId 协作记录ID
     * @return 协作记录
     */
    @GetMapping("/record/{collaborationId}")
    public BaseResponse<CollaborationRecord> getCollaborationRecord(@PathVariable Long collaborationId) {
        CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecord(collaborationId);
        return ResultUtils.success(collaborationRecord);
    }

    /**
     * 根据应用ID获取协作记录
     * @param appId 应用ID
     * @return 协作记录
     */
    @GetMapping("/record/by-app/{appId}")
    public BaseResponse<CollaborationRecord> getCollaborationRecordByAppId(@PathVariable Long appId) {
        CollaborationRecord collaborationRecord = collaborationService.getCollaborationRecordByAppId(appId);
        return ResultUtils.success(collaborationRecord);
    }

    /**
     * 获取在线好友列表
     * @param request 请求对象
     * @return 在线好友列表
     */
    @GetMapping("/online-friends")
    public BaseResponse<List<User>> getOnlineFriends(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<User> onlineFriends = collaborationService.getOnlineFriends(loginUser.getId());
        return ResultUtils.success(onlineFriends);
    }

    /**
     * 获取协作者列表
     * @param collaborationId 协作记录ID
     * @return 协作者列表
     */
    @GetMapping("/members/{collaborationId}")
    public BaseResponse<List<CollaborationMemberVO>> getCollaborationMembers(@PathVariable Long collaborationId) {
        List<CollaborationMemberVO> collaborationMembers = collaborationService.getCollaborationMembers(collaborationId);
        return ResultUtils.success(collaborationMembers);
    }

}