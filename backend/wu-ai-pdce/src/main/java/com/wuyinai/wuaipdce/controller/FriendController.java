package com.wuyinai.wuaipdce.controller;

import com.wuyinai.wuaipdce.annotation.AuthCheck;
import com.wuyinai.wuaipdce.common.BaseResponse;
import com.wuyinai.wuaipdce.common.ResultUtils;
import com.wuyinai.wuaipdce.exception.BusinessException;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.model.dto.friend.FriendRequestDTO;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.FriendRequestVO;
import com.wuyinai.wuaipdce.model.vo.FriendVO;
import com.wuyinai.wuaipdce.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友关系 控制层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 发送好友请求
     *
     * @param requestDTO   好友请求DTO
     * @param request      HttpServletRequest
     * @return             是否发送成功
     */
    @PostMapping("/request")
    public BaseResponse<Boolean> sendFriendRequest(@RequestBody FriendRequestDTO requestDTO, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(requestDTO == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(requestDTO.getToUserId() == null, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        boolean result = friendService.sendFriendRequest(userId, requestDTO);
        return ResultUtils.success(result);
    }

    /**
     * 接受好友请求
     *
     * @param requestId   请求ID
     * @param request      HttpServletRequest
     * @return            是否接受成功
     */
    @PutMapping("/request/{requestId}/accept")
    public BaseResponse<Boolean> acceptFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(requestId == null, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        boolean result = friendService.acceptFriendRequest(userId, requestId);
        return ResultUtils.success(result);
    }

    /**
     * 拒绝好友请求
     *
     * @param requestId   请求ID
     * @param request      HttpServletRequest
     * @return            是否拒绝成功
     */
    @PutMapping("/request/{requestId}/reject")
    public BaseResponse<Boolean> rejectFriendRequest(@PathVariable Long requestId, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(requestId == null, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        boolean result = friendService.rejectFriendRequest(userId, requestId);
        return ResultUtils.success(result);
    }

    /**
     * 获取好友列表
     *
     * @param request      HttpServletRequest
     * @return             好友列表
     */
    @GetMapping("/list")
    public BaseResponse<List<FriendVO>> getFriendList(HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        List<FriendVO> friendList = friendService.getFriendList(userId);
        return ResultUtils.success(friendList);
    }

    /**
     * 删除好友
     *
     * @param friendId   好友ID
     * @param request      HttpServletRequest
     * @return            是否删除成功
     */
    @DeleteMapping("/{friendId}")
    public BaseResponse<Boolean> deleteFriend(@PathVariable Long friendId, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(friendId == null, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        boolean result = friendService.deleteFriend(userId, friendId);
        return ResultUtils.success(result);
    }

    /**
     * 获取好友详情
     *
     * @param friendId   好友ID
     * @param request      HttpServletRequest
     * @return            好友详情
     */
    @GetMapping("/{friendId}")
    public BaseResponse<FriendVO> getFriendDetail(@PathVariable Long friendId, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(friendId == null, ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        FriendVO friendDetail = friendService.getFriendDetail(userId, friendId);
        return ResultUtils.success(friendDetail);
    }

    /**
     * 获取收到的好友请求列表
     *
     * @param request      HttpServletRequest
     * @return             好友请求列表
     */
    @GetMapping("/request/list")
    public BaseResponse<List<FriendRequestVO>> getReceivedFriendRequests(HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        List<FriendRequestVO> requestList = friendService.getReceivedFriendRequests(userId);
        return ResultUtils.success(requestList);
    }

    /**
     * 获取发送的好友请求列表
     *
     * @param request      HttpServletRequest
     * @return             好友请求列表
     */
    @GetMapping("/request/sent")
    public BaseResponse<List<FriendRequestVO>> getSentFriendRequests(HttpServletRequest request) {
        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        List<FriendRequestVO> requestList = friendService.getSentFriendRequests(userId);
        return ResultUtils.success(requestList);
    }

    /**
     * 搜索用户
     *
     * @param keyword   搜索关键词
     * @param request      HttpServletRequest
     * @return            用户列表
     */
    @GetMapping("/search")
    public BaseResponse<List<FriendVO>> searchUsers(@RequestParam String keyword, HttpServletRequest request) {
        // 检查参数
        ThrowUtils.throwIf(keyword == null || keyword.isEmpty(), ErrorCode.PARAMS_ERROR);

        // 获取当前用户ID
        Long userId = getCurrentUserId(request);

        List<FriendVO> userList = friendService.searchUsers(keyword, userId);
        return ResultUtils.success(userList);
    }

    /**
     * 获取当前登录用户ID
     *
     * @param request HttpServletRequest
     * @return 当前登录用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute("user_login");
        User user = (User) userObj;
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user.getId();
    }
}
