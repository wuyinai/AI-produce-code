package com.wuyinai.wuaipdce.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wuyinai.wuaipdce.model.dto.chathistory.ChatHistoryQueryRequest;
import com.wuyinai.wuaipdce.model.entity.ChatHistory;
import com.wuyinai.wuaipdce.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 *
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 保存对话历史
     *
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 关联删除，
     * 当应用被删除时，连带对话信息全部被删除
     */
    boolean deleteMessage(Long appId);

    /**
     * 获取查询包装类
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 游标查询服务方法
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime lastCreateTime, User loginUser);

}
