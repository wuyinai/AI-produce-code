package com.wuyinai.wuaipdce.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.wuyinai.wuaipdce.constant.UserConstant;
import com.wuyinai.wuaipdce.model.entity.User;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * WebSocket握手拦截器
 * 用于验证用户身份并将用户信息保存到WebSocket会话中
 * 
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
public class WebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 从HttpSession中获取登录用户信息
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession();
            User loginUser = (User) session.getAttribute(UserConstant.USER_LOGIN_STATE);
            if (loginUser != null) {
                // 将用户ID保存到WebSocket会话属性中
                attributes.put("userId", loginUser.getId());
                return true;
            }
        }
        // 未登录用户拒绝连接
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后无需处理
    }
}
