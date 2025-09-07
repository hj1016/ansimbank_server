package com.grandma.ansimbank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yml의 clova.stt.* 설정 값을 자바 객체로 받아옵니다.
 */
@Component
@ConfigurationProperties(prefix = "clova.stt")
@Getter
@Setter
public class ClovaSttProperties {
    private String url;
    private String clientId;
    private String clientSecret;
}
