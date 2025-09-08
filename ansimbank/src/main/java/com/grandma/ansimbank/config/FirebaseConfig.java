package com.grandma.ansimbank.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;

//@Configuration  // Firebase 설정 파일이 없어서 임시로 비활성화
public class FirebaseConfig {

    @Value("${firebase.service-account-key}")
    private Resource serviceAccountKey;

    @PostConstruct
    public void initialize() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountKey.getInputStream()))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}