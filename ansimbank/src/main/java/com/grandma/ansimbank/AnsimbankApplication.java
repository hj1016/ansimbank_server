package com.grandma.ansimbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * 주의: 개발 편의를 위해 보안 설정이 임시로 비활성화되어 있습니다.
 * 
 * @SpringBootApplication의 exclude 파라미터가 Spring Security 자동 설정을 비활성화하여
 * 모든 API 엔드포인트가 인증 없이 접근 가능한 상태입니다.
 * 
 * JWT 인증 구현 시 exclude 파라미터를 반드시 제거해야 합니다:
 * @SpringBootApplication(exclude = {SecurityAutoConfiguration.class})  <- 이 줄 삭제
 * @SpringBootApplication                                                <- 이렇게 변경
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class AnsimbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnsimbankApplication.class, args);
	}

}
