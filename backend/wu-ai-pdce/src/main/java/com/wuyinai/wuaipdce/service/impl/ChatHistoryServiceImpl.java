package com.wuyinai.wuaipdce.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wuyinai.wuaipdce.constant.UserConstant;
import com.wuyinai.wuaipdce.exception.ErrorCode;
import com.wuyinai.wuaipdce.exception.ThrowUtils;
import com.wuyinai.wuaipdce.mapper.ChatHistoryMapper;
import com.wuyinai.wuaipdce.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wuyinai.wuaipdce.model.entity.App;
import com.wuyinai.wuaipdce.model.entity.ChatHistory;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.enums.ChatHistoryMessageTypeEnum;
import com.wuyinai.wuaipdce.service.AppService;
import com.wuyinai.wuaipdce.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService {


    @Autowired
    @Lazy//它通过延迟加载解决了循环依赖问题
    private AppService appService;

    /**
     * 添加对话历史
     *
     * @param appId       应用id
     * @param message     消息
     * @param messageType 消息类型
     * @param userId      用户id
     * @return 是否保存成功
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        //校验参数
        ThrowUtils.throwIf(appId == null||appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null||userId <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
        //验证消息类型是否有效（配对枚举值）
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型错误");
        //创建对象
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        //返回（保存对象）
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteMessage(Long appId) {
        //校验参数
        ThrowUtils.throwIf(appId == null||appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        //获取所有先关内容
        QueryWrapper queryWrapper = new QueryWrapper().create()
                .eq("appId", appId);
        //全部删除
        return this.remove(queryWrapper);
    }


    /**
     * 获取查询包装类
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        //创建一个QueryWrapper对象
        QueryWrapper queryWrapper = QueryWrapper.create();
        //参数校验
         if (chatHistoryQueryRequest == null){
             return queryWrapper;
         }
        //获取请求体内容
        Long id = chatHistoryQueryRequest.getId();
         String message = chatHistoryQueryRequest.getMessage();
         String messageType = chatHistoryQueryRequest.getMessageType();
         Long appId = chatHistoryQueryRequest.getAppId();
         Long userId = chatHistoryQueryRequest.getUserId();
         LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
         String sortField = chatHistoryQueryRequest.getSortField();//继承与PageRequest
         String sortOrder = chatHistoryQueryRequest.getSortOrder();//继承与PageRequest

        //拼接查询
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);

        //游标查询逻辑 --只使用createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
            //lt添加<条件，createTime 小于指定时间，实现游标查询。
        }
        //排序
        if (StrUtil.isNotBlank(sortField)){
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        }else{
            //默认按创建时间降序排序
             queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 游标查询服务。获取应用对话历史
     * @param appId
     * @param pageSize
     * @param lastCreateTime
     * @param loginUser
     * @return
     */
    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser) {
        //校验参数
        ThrowUtils.throwIf(appId == null||appId <= 0, ErrorCode.PARAMS_ERROR, "应用id不能为空");
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小不能小于0");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        //验证权限（本人或者管理员可以操作）
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权限操作");
        //构建查询条件
         ChatHistoryQueryRequest chatHistoryQueryRequest = new ChatHistoryQueryRequest();
         chatHistoryQueryRequest.setAppId(appId);
         chatHistoryQueryRequest.setLastCreateTime(lastCreateTime);
         QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        //查询数据
         return this.page(Page.of(1, pageSize), queryWrapper);
    }


}
