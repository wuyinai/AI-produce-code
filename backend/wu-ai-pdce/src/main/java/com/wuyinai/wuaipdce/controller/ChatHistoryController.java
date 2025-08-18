package com.wuyinai.wuaipdce.controller;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wuyinai.wuaipdce.annotation.AuthCheck;
import com.wuyinai.wuaipdce.common.BaseResponse;
import com.wuyinai.wuaipdce.common.ResultUtils;
import com.wuyinai.wuaipdce.constant.UserConstant;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wuyinai.wuaipdce.model.entity.ChatHistory;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.service.ChatHistoryService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              // 默认每页为10条记录
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                              //required = false,表示参数是可选的
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        if (StrUtil.isEmpty(chatHistoryQueryRequest.getMessage())){
            chatHistoryQueryRequest.setMessage(null);
        }
        if (StrUtil.isEmpty(chatHistoryQueryRequest.getMessageType())){
            chatHistoryQueryRequest.setMessageType(null);
        }
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }


}
