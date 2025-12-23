package com.wuyinai.wuaipdce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.wuyinai.wuaipdce.websocket.WebSocketHandler;
import com.wuyinai.wuaipdce.websocket.WebSocketInterceptor;

import jakarta.annotation.Resource;

/**
 * WebSocket配置类
 * 
 * @author <a href="https://github.com/wuyinai">程序实习生奈奈</a>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket处理器，设置路径和允许跨域
        registry.addHandler(webSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .addInterceptors(new WebSocketInterceptor());
    }
}
