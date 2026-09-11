package com.grandma.ansimbank.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
public class FirebaseConfig {

    private final Resource credentials;

    public FirebaseConfig(@Value("${firebase.credentials-path}") Resource credentials) {
        this.credentials = credentials;
    }

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentials.getInputStream()))
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK 초기화 완료");
            }
        } catch (IOException e) {
            System.err.println("Firebase 초기화 실패: " + e.getMessage());
        }
    }
}
