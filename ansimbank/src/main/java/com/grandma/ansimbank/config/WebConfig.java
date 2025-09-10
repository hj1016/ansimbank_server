package com.grandma.ansimbank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/로 시작하는 모든 경로에 대해
                // 👇 중요: 만약 리액트 앱의 주소가 3000번이 아니면 이 부분을 수정해야 해!
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // ✨ 이게 핵심! 출입증(쿠키)을 통한 세션 유지를 허용
                .maxAge(3600);
    }
}
