package com.njfu.chat.config.webSocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${allowedOrigins.test}")
    private String testOrigin;

    @Value("${allowedOrigins.remote}")
    private String remoteOrigin;

    /**
     * 注册WebSocket处理器
     * 配置处理器、拦截器、允许域、SockJs支持
     *
     * @param registry registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        // 设置允许域，当请求的RequestHeaders中的Origin不在允许范围内，禁止连接
        String[] allowedOrigins = {testOrigin, remoteOrigin};

        registry.addHandler(chatHandler(), "/chatHandler")
                .addInterceptors(chatHandshakeInterceptor())
                .setAllowedOrigins(allowedOrigins);

        // 当浏览器不支持WebSocket，使用SockJs支持
        registry.addHandler(chatHandler(), "/sockjs-chatHandler")
                .addInterceptors(chatHandshakeInterceptor())
                .setAllowedOrigins(allowedOrigins)
                .withSockJS();
    }

    @Bean
    public ChatHandler chatHandler() {
        return new ChatHandler();
    }

    @Bean
    public ChatHandshakeInterceptor chatHandshakeInterceptor() {
        return new ChatHandshakeInterceptor();
    }
}
