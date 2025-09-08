package com.grandma.ansimbank.fcm.repository;

import com.grandma.ansimbank.fcm.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    // 사용자 ID로 활성 토큰들 찾기 (가장 중요!)
    List<FcmToken> findByUserUserIdAndIsActiveTrue(Long userId);

    // 특정 FCM 토큰 찾기
    Optional<FcmToken> findByFcmToken(String fcmToken);

}