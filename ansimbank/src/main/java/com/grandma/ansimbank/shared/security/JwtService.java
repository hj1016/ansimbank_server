package com.grandma.ansimbank.shared.security;

import org.springframework.stereotype.Service;

@Service
public class JwtService {
    public Long validateAndGetUserId(String token) {
        // TODO: 실제 서명/만료 검증으로 교체
        if (token != null && token.startsWith("uid:")) {
            return Long.valueOf(token.substring(4));
        }
        return null;
    }
}
