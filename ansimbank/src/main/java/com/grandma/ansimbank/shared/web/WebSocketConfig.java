package com.grandma.ansimbank.shared.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
            .setAllowedOriginPatterns("http://localhost:3000")
            .withSockJS(); // 네이티브 WS만 쓸 거면 제거
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // 클라→서버(커맨드)
        registry.setUserDestinationPrefix("/user");         // per-user 큐
        registry.enableSimpleBroker("/topic", "/queue");    // 서버→클라(이벤트)
    }
}
