package com.hobbyswap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 設定 WebSocket 連線端點，前端 JS 會連線到這裡
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 設定訊息廣播的路徑前綴
        registry.enableSimpleBroker("/topic");
        // 設定客戶端發送訊息的路徑前綴
        registry.setApplicationDestinationPrefixes("/app");
        // 啟用使用者專屬前綴 (預設就是 /user，這裡顯式宣告更清楚)
        registry.setUserDestinationPrefix("/user");
    }
}