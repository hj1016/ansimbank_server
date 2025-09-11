package com.grandma.ansimbank.shared.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    public JwtHandshakeInterceptor(JwtService jwtService) { this.jwtService = jwtService; }

    @Override
    public boolean beforeHandshake(ServerHttpRequest req, ServerHttpResponse res,
        WebSocketHandler ws, Map<String, Object> attrs) {
        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
        if (token == null && req instanceof ServletServerHttpRequest s) {
            token = s.getServletRequest().getParameter("access_token");
        }
        Long userId = jwtService.validateAndGetUserId(token);
        if (userId == null) return false;

        attrs.put("principal", (Principal) () -> String.valueOf(userId));
        return true;
    }

    @Override public void afterHandshake(ServerHttpRequest a, ServerHttpResponse b, WebSocketHandler c, Exception d) {}
}
