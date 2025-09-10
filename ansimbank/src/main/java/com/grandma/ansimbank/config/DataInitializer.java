package com.grandma.ansimbank.config;

import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.fcm.repository.FamilyConnectionRepository;
import com.grandma.ansimbank.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FamilyConnectionRepository familyConnectionRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 기존 데이터가 있으면 목데이터 생성 스킵
        if (userRepository.count() > 0) {
            log.info("기존 데이터가 있어서 목데이터 생성을 스킵합니다.");
            return;
        }

        log.info("테스트용 목데이터를 생성합니다...");

        // 1. 사용자 데이터 생성
        User parent = User.builder()
                .email("parent@test.com")
                .password("$2a$10$example.hash.for.password123") // 실제로는 BCrypt 해시 사용
                .name("김부모")
                .phone("010-1234-5678")
                .birthDate(LocalDate.of(1980, 5, 15))
                .userType(User.UserType.PARENT)
                .socialProvider(User.SocialProvider.NONE)
                .isActive(true)
                .build();

        User child = User.builder()
                .email("child@test.com")
                .password("$2a$10$example.hash.for.password123")
                .name("김자녀")
                .phone("010-9876-5432")
                .birthDate(LocalDate.of(2000, 8, 20))
                .userType(User.UserType.CHILD)
                .socialProvider(User.SocialProvider.NONE)
                .isActive(true)
                .build();

        User testUser = User.builder()
                .email("test@test.com")
                .password("$2a$10$example.hash.for.password123")
                .name("테스트사용자")
                .phone("010-1111-2222")
                .birthDate(LocalDate.of(1975, 12, 10))
                .userType(User.UserType.PARENT)
                .socialProvider(User.SocialProvider.NONE)
                .isActive(true)
                .build();

        // 카카오 소셜 로그인 사용자
        User kakaoUser = User.builder()
                .email("kakao@test.com")
                .password("$2a$10$example.hash.for.password123")
                .name("카카오사용자")
                .phone("010-3333-4444")
                .birthDate(LocalDate.of(1990, 3, 25))
                .userType(User.UserType.PARENT)
                .socialProvider(User.SocialProvider.KAKAO)
                .socialId("kakao123456")
                .isActive(true)
                .build();

        // 사용자 저장
        parent = userRepository.save(parent);
        child = userRepository.save(child);
        testUser = userRepository.save(testUser);

        // 2. 가족 연동 관계 생성 (승인된 상태)
        FamilyConnection approvedConnection = FamilyConnection.builder()
                .parent(parent)
                .child(child)
                .connectionStatus(FamilyConnection.ConnectionStatus.APPROVED)
                .build();

        familyConnectionRepository.save(approvedConnection);

        log.info("목데이터 생성 완료!");
        log.info("생성된 사용자:");
        log.info("- 부모: ID={}, 이름={}, 이메일={}", parent.getUserId(), parent.getName(), parent.getEmail());
        log.info("- 자녀: ID={}, 이름={}, 이메일={}", child.getUserId(), child.getName(), child.getEmail());
        log.info("- 테스트: ID={}, 이름={}, 이메일={}", testUser.getUserId(), testUser.getName(), testUser.getEmail());
        log.info("가족 연동 관계:");
        log.info("- 승인됨: 부모ID={} <-> 자녀ID={}", parent.getUserId(), child.getUserId());
    }
}