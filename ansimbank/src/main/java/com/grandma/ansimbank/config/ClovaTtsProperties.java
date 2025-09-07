package com.grandma.ansimbank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * application.yml의 clova.tts 설정값을 담는 클래스
 */
@Component
@ConfigurationProperties(prefix = "clova.tts")
@Getter
@Setter
public class ClovaTtsProperties {
    private String url;
    private String clientId;
    private String clientSecret;
}
