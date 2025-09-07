package com.grandma.ansimbank.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// TODO: JWT 도입 시 @Configuration 주석 해제
//@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws-stomp/**").permitAll()       // ✅ WebSocket 허용
                .requestMatchers("/stomp-test.html").permitAll()   // ✅ 테스트 페이지 허용
                .requestMatchers("/api/demo/**").permitAll()       // ✅ Demo API 허용 (테스트용)
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())   // ✅ 기본 로그인폼 꺼버리기
            .httpBasic(basic -> basic.disable()); // ✅ BasicAuth 팝업 꺼버리기
        return http.build();
    }
}
