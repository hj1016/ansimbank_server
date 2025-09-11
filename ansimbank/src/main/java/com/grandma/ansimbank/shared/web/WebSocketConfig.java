package com.grandma.ansimbank.shared.web;

import com.grandma.ansimbank.shared.security.JwtHandshakeInterceptor;
import com.grandma.ansimbank.shared.security.PrincipalHandshakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final PrincipalHandshakeHandler principalHandshakeHandler;
    public WebSocketConfig(JwtHandshakeInterceptor i, PrincipalHandshakeHandler h) {
        this.jwtHandshakeInterceptor = i; this.principalHandshakeHandler = h;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
            .addInterceptors(jwtHandshakeInterceptor)
            .setHandshakeHandler(principalHandshakeHandler)
            .setAllowedOriginPatterns("http://localhost:3000")
            .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue");
    }
}
