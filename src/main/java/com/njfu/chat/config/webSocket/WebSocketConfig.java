package com.njfu.chat.config.webSocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    /**
     * 配置WebSocket处理器
     *
     * @param registry
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler(), "/chatHandler")
                .addInterceptors(chatHandshakeInterceptor())
                .setAllowedOrigins("http://localhost");
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
