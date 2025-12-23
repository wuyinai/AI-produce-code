package com.wuyinai.wuaipdce.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuyinai.wuaipdce.model.entity.User;
import com.wuyinai.wuaipdce.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
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
            // 可以添加其他类型消息的处理逻辑
        } catch (Exception e) {
            log.error("处理用户 {} 消息失败：{}", userId, e.getMessage(), e);
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
     * 向指定用户发送消息
     * 
     * @param userId 用户ID
     * @param message 消息内容
     */
    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = ONLINE_USERS.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("向用户 {} 发送消息失败：{}", userId, e.getMessage());
            }
        }
    }

    /**
     * 向所有在线用户发送消息
     * 
     * @param message 消息内容
     */
    public void sendMessageToAllUsers(String message) {
        for (WebSocketSession session : ONLINE_USERS.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("向用户发送消息失败：{}", e.getMessage());
                }
            }
        }
    }
}
