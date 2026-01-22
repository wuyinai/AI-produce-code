package com.wuyinai.wuaipdce.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuyinai.wuaipdce.mapper.CollaborationMemberMapper;
import com.wuyinai.wuaipdce.model.entity.CollaborationMember;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.model.vo.UserVO;
import com.wuyinai.wuaipdce.service.CollaborationService;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket处理器
 * 处理WebSocket连接、断开和消息
 * 
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Component
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {

    // 存储在线用户的WebSocket会话，key为用户ID
    public static final Map<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();
    
    // 存储用户最后活跃时间，用于心跳检测
    private static final Map<Long, Long> USER_LAST_ACTIVE_TIME = new ConcurrentHashMap<>();
    
    // 心跳超时时间（毫秒），缩短为20秒
    private static final long HEARTBEAT_TIMEOUT = 20000;
    
    // 心跳检测间隔（毫秒），缩短为10秒
    private static final long HEARTBEAT_CHECK_INTERVAL = 10000;
    
    // JSON对象映射器
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    // 定时任务执行器，用于心跳检测
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    @Resource
    private UserService userService;
    
    @Resource
    private CollaborationService collaborationService;

    /**
     * 构造方法，初始化心跳检测
     */
    public WebSocketHandler() {
        // 初始化心跳检测任务
        heartbeatExecutor.scheduleAtFixedRate(this::checkHeartbeat, HEARTBEAT_CHECK_INTERVAL, HEARTBEAT_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 建立WebSocket连接时调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            // 将用户会话添加到在线用户映射中
            ONLINE_USERS.put(userId, session);
            // 更新用户最后活跃时间
            USER_LAST_ACTIVE_TIME.put(userId, System.currentTimeMillis());
            
            log.info("用户 {} 建立WebSocket连接，当前在线人数：{}", userId, ONLINE_USERS.size());
            
            // 更新用户在线状态为在线
            User user = userService.getById(userId);
            if (user != null) {
                user.setOnlineStatus("online");
                userService.updateById(user);
                
                // 可以发送在线状态更新通知给好友，这里暂时省略
            }
        }
    }

    /**
     * 断开WebSocket连接时调用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            // 从在线用户映射中移除用户会话
            ONLINE_USERS.remove(userId);
            // 移除用户最后活跃时间记录
            USER_LAST_ACTIVE_TIME.remove(userId);
            
            log.info("用户 {} 断开WebSocket连接，当前在线人数：{}", userId, ONLINE_USERS.size());
            
            // 更新用户在线状态为离线
            User user = userService.getById(userId);
            if (user != null) {
                user.setOnlineStatus("offline");
                userService.updateById(user);
                
                // 可以发送在线状态更新通知给好友，这里暂时省略
            }
        }
    }

    /**
     * 处理收到的WebSocket消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从会话属性中获取用户ID
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        
        // 更新用户最后活跃时间
        USER_LAST_ACTIVE_TIME.put(userId, System.currentTimeMillis());
        
        String payload = message.getPayload();
        log.info("收到用户 {} 消息：{}", userId, payload);
        
        try {
            // 解析消息
            Map<String, Object> messageData = OBJECT_MAPPER.readValue(payload, Map.class);
            String type = (String) messageData.get("type");
            
            // 处理心跳消息
            if ("heartbeat".equals(type)) {
                // 回复心跳响应
                Map<String, Object> response = new ConcurrentHashMap<>();
                response.put("type", "heartbeat");
                response.put("timestamp", System.currentTimeMillis());
                session.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(response)));
            }
            // 处理协作邀请消息
            else if ("collaboration_invite".equals(type)) {
                handleCollaborationInvite(messageData, userId);
            }
            // 处理协作邀请接受消息
            else if ("collaboration_accept".equals(type)) {
                handleCollaborationAccept(messageData, String.valueOf(userId));
            }
            // 处理协作邀请拒绝消息
            else if ("collaboration_reject".equals(type)) {
                handleCollaborationReject(messageData, userId);
            }
        } catch (Exception e) {
            log.error("处理用户 {} 消息失败：{}", userId, e.getMessage(), e);
        }
    }
    //发送AI流式回答共享消息（带会话ID，用于前端合并消息）
    public void handleAiAnswerStreamShare(String sessionId, String chunkContent, List<Long> userIds, Long appId, Long senderId, String senderName) {
        try{
            log.info("AI流式回答共享消息，sessionId: {}, chunk: {}", sessionId, chunkContent);
            //构建消息
            Map<Object, Object> streamMessage = new ConcurrentHashMap<>();
            streamMessage.put("type", "ai_answer_stream");
            streamMessage.put("sessionId", sessionId);
            streamMessage.put("chunk", chunkContent);
            streamMessage.put("appId", appId);
            streamMessage.put("senderId", senderId);
            streamMessage.put("senderName", senderName);

            //发送AI流式回答消息给协作者们
            for (Long userId : userIds) {
                WebSocketSession session = ONLINE_USERS.get(userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(streamMessage)));
                }
            }
        }catch (IOException e){
            log.error("发送AI流式回答共享消息失败：{}", e.getMessage(), e);
        }
    }

    //发送AI流式回答结束消息
    public void handleAiAnswerStreamEnd(String sessionId, String fullContent, List<Long> userIds, Long appId, Long senderId, String senderName) {
        try{
            log.info("AI流式回答结束消息，sessionId: {}", sessionId);
            //构建结束消息
            Map<Object, Object> endMessage = new ConcurrentHashMap<>();
            endMessage.put("type", "ai_answer_stream_end");
            endMessage.put("sessionId", sessionId);
            endMessage.put("fullContent", fullContent);
            endMessage.put("appId", appId);
            endMessage.put("senderId", senderId);
            endMessage.put("senderName", senderName);

            //发送结束消息给协作者们
            for (Long userId : userIds) {
                WebSocketSession session = ONLINE_USERS.get(userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(endMessage)));
                }
            }
        }catch (IOException e){
            log.error("发送AI流式回答结束消息失败：{}", e.getMessage(), e);
        }
    }

    //发送协作用户发送给的消息
    public void handleCollaborationMessage(String message, List<Long> userIds, Long appId, UserVO sender) {
        try{
            log.info("协作消息{}", message);
            //构建消息
            Map<Object, Object> collaborationMessage = new ConcurrentHashMap<>();
            collaborationMessage.put("type", "collaboration_message");
            collaborationMessage.put("message", message);
            collaborationMessage.put("user",sender);
            collaborationMessage.put("appId", appId);

            //发送消息给协作者们
            for (Long userId : userIds) {
                WebSocketSession session = ONLINE_USERS.get(userId);
                if (session != null && session.isOpen()) {
                    session.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(collaborationMessage)));
                }
            }
        }catch (IOException e){
            log.error("发送协作消息失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 心跳检测
     */
    private void checkHeartbeat() {
        long currentTime = System.currentTimeMillis();
        
        log.info("开始心跳检测，当前在线用户数：{}，当前时间：{}", USER_LAST_ACTIVE_TIME.size(), currentTime);
        
        // 遍历所有在线用户，检查心跳
        for (Map.Entry<Long, Long> entry : USER_LAST_ACTIVE_TIME.entrySet()) {
            Long userId = entry.getKey();
            Long lastActiveTime = entry.getValue();
            
            log.info("检查用户 {}，最后活跃时间：{}，当前时间：{}，超时时间：{}", 
                    userId, lastActiveTime, currentTime, HEARTBEAT_TIMEOUT);
            
            // 如果超过心跳超时时间，关闭连接
            if (currentTime - lastActiveTime > HEARTBEAT_TIMEOUT) {
                log.warn("用户 {} 心跳超时，关闭连接，超时时间：{}毫秒", userId, currentTime - lastActiveTime);
                
                // 关闭WebSocket连接
                WebSocketSession session = ONLINE_USERS.get(userId);
                if (session != null && session.isOpen()) {
                    log.info("正在关闭用户 {} 的WebSocket连接", userId);
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                        log.info("成功关闭用户 {} 的WebSocket连接", userId);
                    } catch (IOException e) {
                        log.error("关闭用户 {} 连接失败：{}", userId, e.getMessage(), e);
                    }
                }
                
                // 更新用户状态为离线
                log.info("开始更新用户 {} 状态为离线", userId);
                updateUserOfflineStatus(userId);
                
                // 清理用户数据
                log.info("清理用户 {} 的在线数据", userId);
                ONLINE_USERS.remove(userId);
                USER_LAST_ACTIVE_TIME.remove(userId);
                log.info("用户 {} 数据清理完成", userId);
            } else {
                log.info("用户 {} 心跳正常，活跃时间差：{}毫秒", userId, currentTime - lastActiveTime);
            }
        }
        
        log.info("心跳检测结束，剩余在线用户数：{}", USER_LAST_ACTIVE_TIME.size());
    }

    /**
     * 更新用户状态为离线
     * 
     * @param userId 用户ID
     */
    private void updateUserOfflineStatus(Long userId) {
        log.info("开始查询用户 {} 信息", userId);
        User user = userService.getById(userId);
        if (user != null) {
            log.info("查询到用户 {} 信息，当前状态：{}", userId, user.getOnlineStatus());
            user.setOnlineStatus("offline");
            log.info("准备更新用户 {} 状态为离线", userId);
            boolean updateResult = userService.updateById(user);
            if (updateResult) {
                log.info("用户 {} 状态更新为离线成功", userId);
                // 再次查询确认更新结果
                User updatedUser = userService.getById(userId);
                if (updatedUser != null) {
                    log.info("确认用户 {} 更新后的状态：{}", userId, updatedUser.getOnlineStatus());
                }
            } else {
                log.error("用户 {} 状态更新为离线失败", userId);
            }
        } else {
            log.error("未找到用户 {} 信息", userId);
        }
    }

    /**
     * 处理协作邀请消息
     * 
     * @param messageData 消息数据
     * @param senderId 发送者ID
     */
    private void handleCollaborationInvite(Map<String, Object> messageData, Long senderId) throws IOException {
        // 解析消息数据
        Long receiverId = ((Number) messageData.get("receiverId")).longValue();
        Long appId = ((Number) messageData.get("appId")).longValue();
        String appName = (String) messageData.get("appName");
        Long collaborationId = ((Number) messageData.get("collaborationId")).longValue();
        
        // 构建邀请消息
        Map<String, Object> inviteMessage = new ConcurrentHashMap<>();
        inviteMessage.put("type", "collaboration_invite");
        inviteMessage.put("senderId", senderId);
        inviteMessage.put("appId", appId);
        inviteMessage.put("appName", appName);
        inviteMessage.put("collaborationId", collaborationId);
        inviteMessage.put("timestamp", System.currentTimeMillis());
        
        // 发送邀请消息给接收者
        WebSocketSession receiverSession = ONLINE_USERS.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(inviteMessage)));
            log.info("向用户 {} 发送协作邀请消息成功", receiverId);
        } else {
            log.warn("用户 {} 不在线，无法发送协作邀请消息", receiverId);
        }
    }

    /**
     * 处理协作邀请接受消息
     * 
     * @param messageData 消息数据
     * @param receiverId 接收者ID
     */
    private void handleCollaborationAccept(Map<String, Object> messageData, String receiverId) throws IOException {
        // 解析消息数据
        Long senderId = Long.valueOf(messageData.get("senderId").toString());
        Long collaborationId = Long.valueOf(messageData.get("collaborationId").toString());
        
        // 添加用户为协作者
        CollaborationMember collaborationMember = CollaborationMember.builder()
                .collaborationId(collaborationId)
                .userId(Long.valueOf(receiverId))
                .joinTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();
        collaborationService.save(collaborationMember);
        
        // 构建接受消息
        Map<String, Object> acceptMessage = new ConcurrentHashMap<>();
        acceptMessage.put("type", "collaboration_accept");
        acceptMessage.put("senderId", senderId);
        acceptMessage.put("receiverId", receiverId);
        acceptMessage.put("collaborationId", collaborationId);
        acceptMessage.put("timestamp", System.currentTimeMillis());
        
        // 发送接受消息给发送者
        WebSocketSession senderSession = ONLINE_USERS.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(acceptMessage)));
            log.info("向用户 {} 发送协作邀请接受消息成功", senderId);
        } else {
            log.warn("用户 {} 不在线，无法发送协作邀请接受消息", senderId);
        }
    }

    /**
     * 处理协作邀请拒绝消息
     * 
     * @param messageData 消息数据
     * @param receiverId 接收者ID
     */
    private void handleCollaborationReject(Map<String, Object> messageData, Long receiverId) throws IOException {
        // 解析消息数据
        Long senderId = ((Number) messageData.get("senderId")).longValue();
        Long collaborationId = ((Number) messageData.get("collaborationId")).longValue();
        String reason = (String) messageData.get("reason");
        
        // 构建拒绝消息
        Map<String, Object> rejectMessage = new ConcurrentHashMap<>();
        rejectMessage.put("type", "collaboration_reject");
        rejectMessage.put("senderId", senderId);
        rejectMessage.put("receiverId", receiverId);
        rejectMessage.put("collaborationId", collaborationId);
        rejectMessage.put("reason", reason);
        rejectMessage.put("timestamp", System.currentTimeMillis());
        
        // 发送拒绝消息给发送者
        WebSocketSession senderSession = ONLINE_USERS.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(rejectMessage)));
            log.info("向用户 {} 发送协作邀请拒绝消息成功", senderId);
        } else {
            log.warn("用户 {} 不在线，无法发送协作邀请拒绝消息", senderId);
        }
    }

    /**
     * 发送协作邀请消息
     * 
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param appId 应用ID
     * @param appName 应用名称
     * @param collaborationId 协作记录ID
     */
    public void sendCollaborationInvite(Long senderId, Long receiverId,String userName, Long appId, String appName, Long collaborationId,String userAvatar) {
        try {
            // 构建邀请消息
            Map<String, Object> inviteMessage = new ConcurrentHashMap<>();
            inviteMessage.put("type", "collaboration_invite");
            inviteMessage.put("senderId", String.valueOf(senderId));
            inviteMessage.put("appId", String.valueOf(appId));
            inviteMessage.put("appName", appName);
            inviteMessage.put("collaborationId", String.valueOf(collaborationId));
            inviteMessage.put("userName", userName);
            inviteMessage.put("senderAvatar",userAvatar);
            inviteMessage.put("timestamp", System.currentTimeMillis());
            
            // 发送邀请消息给接收者
            WebSocketSession receiverSession = ONLINE_USERS.get(receiverId);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(inviteMessage)));
                log.info("向用户 {} 发送协作邀请消息成功", receiverId);
            } else {
                log.warn("用户 {} 不在线，无法发送协作邀请消息", receiverId);
            }
        } catch (IOException e) {
            log.error("发送协作邀请消息失败：{}", e.getMessage(), e);
        }
    }
}
