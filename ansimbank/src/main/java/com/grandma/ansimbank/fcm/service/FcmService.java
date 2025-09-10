package com.grandma.ansimbank.fcm.service;

import com.google.firebase.messaging.*;
import com.grandma.ansimbank.fcm.dto.DelegationNotificationRequest;
import com.grandma.ansimbank.fcm.dto.FcmNotificationRequest;
import com.grandma.ansimbank.fcm.entity.FamilyConnection;
import com.grandma.ansimbank.fcm.entity.FcmToken;
import com.grandma.ansimbank.user.User;
import com.grandma.ansimbank.user.repository.FamilyConnectionRepository;
import com.grandma.ansimbank.fcm.repository.FcmTokenRepository;
import com.grandma.ansimbank.user.UserRepository;
import com.grandma.ansimbank.fcm.template.FcmMessageTemplate;
import com.grandma.ansimbank.common.constants.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    /*
    * 1. fcm 토큰 등록: registerToken
    * 2. 사용자에게 fcm 알림 전송: sendNotificationToUser
    * 3.  */

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final FamilyConnectionRepository familyConnectionRepository;
    private final FcmMessageTemplate fcmMessageTemplate;


    /**
     * 사용자에게 FCM 알림 전송
     * @param userId 수신자 사용자 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @return 발송 성공 여부
     */
    @Transactional
    public boolean sendNotificationToUser(Long userId, String title, String body) {
        // 1. 사용자의 활성 토큰들 조회
        List<FcmToken> activeTokens = fcmTokenRepository.findByUserUserIdAndIsActiveTrue(userId);

        if (activeTokens.isEmpty()) {
            log.warn("사용자 {}의 활성 FCM 토큰이 없습니다.", userId);
            return false;
        }

        boolean anySuccess = false;

        // 2. 각 토큰으로 메시지 발송
        for (FcmToken fcmToken : activeTokens) {
            try {
                boolean success = sendSingleMessage(fcmToken.getFcmToken(), title, body);
                if (success) {
                    anySuccess = true;
                    log.info("FCM 발송 성공: userId={}, token={}", userId, maskToken(fcmToken.getFcmToken()));
                }
            } catch (Exception e) {
                log.error("FCM 발송 실패: userId={}, token={}, error={}",
                        userId, maskToken(fcmToken.getFcmToken()), e.getMessage());

                // 토큰이 유효하지 않은 경우 비활성화
                if (isInvalidTokenError(e)) {
                    deactivateToken(fcmToken);
                }
            }
        }

        return anySuccess;
    }

    /**
     * 단일 토큰으로 FCM 메시지 발송
     */
    private boolean sendSingleMessage(String token, String title, String body) {
        try {
            // 목 토큰인 경우 실제 전송 건너뛰기
            if (token.startsWith("test_")) {
                log.info("목 토큰 감지 - 실제 전송 건너뛰고 성공 처리: {}", maskToken(token));
                return true;
            }
            // FCM 메시지 생성
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(token)
                    .build();

            // Firebase로 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.debug("FCM 응답: {}", response);

            return true;

        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패: {}", e.getMessage());
            throw new RuntimeException("FCM 전송 실패", e);
        }
    }

    /**
     * FCM 토큰 등록
     */
   @Transactional
    public void registerToken(Long userId, String fcmToken) {
        Optional<FcmToken> existingToken = fcmTokenRepository.findByFcmToken(fcmToken);

        if (existingToken.isPresent()) {
            // 기존 토큰이 있으면 활성화만 업데이트
            existingToken.get().setIsActive(true);
            fcmTokenRepository.save(existingToken.get());
            log.info("기존 토큰 활성화: {}", maskToken(fcmToken));
        } else {
            // 새 토큰이면 저장 - userRepository 사용해야 함
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            FcmToken newToken = FcmToken.builder()
                    .user(user)
                    .fcmToken(fcmToken)
                    .isActive(true)
                    .build();

            fcmTokenRepository.save(newToken);
            log.info("새 FCM 토큰 등록: userId={}, token={}", userId, maskToken(fcmToken));
        }
    }

    /**
     * 위임업무 알림 발송 (통합 메서드)
     */
    public boolean sendDelegationNotification(DelegationNotificationRequest request) {
        // 1. 알림 대상 결정 (부모)
        Long targetUserId = getNotificationTargetUserId(request.getChildUserId(), request.getParentUserId());

        // 2. 메시지 생성
        FcmNotificationRequest notification = fcmMessageTemplate.createDelegationNotification(request, targetUserId);

        // 3. 알림 발송
        return sendNotificationToUser(notification.getUserId(), notification.getTitle(), notification.getMessage());
    }


    /**
     * 알림을 받을 대상 사용자 ID 결정 (부모-자식 관계 확인)
     */
    private Long getNotificationTargetUserId(Long childUserId, Long parentUserId) {
        // 1. parentUserId가 명시되어 있으면 관계 검증 후 사용
        if (parentUserId != null) {
            Optional<FamilyConnection> connection = familyConnectionRepository
                    .findByParentUserIdAndChildUserId(parentUserId, childUserId);

            if (connection.isPresent() &&
                    connection.get().getConnectionStatus() == ConnectionStatus.APPROVED) {
                return parentUserId;
            } else {
                throw new IllegalArgumentException("유효하지 않은 부모-자식 관계입니다.");
            }
        }

        // 2. parentUserId가 없으면 자식의 부모 찾기
        Optional<FamilyConnection> parentConnection = familyConnectionRepository
                .findByChildUserIdAndConnectionStatus(childUserId, ConnectionStatus.APPROVED);

        if (parentConnection.isPresent()) {
            return parentConnection.get().getParent().getUserId();
        } else {
            throw new IllegalArgumentException("연결된 부모를 찾을 수 없습니다. childUserId: " + childUserId);
        }
    }


    /**
     * 부모의 사기계좌 송금 시도 시 자식들에게 경고 알림
     */
    public boolean sendFraudWarningToChildren(Long parentUserId, String accountNumber, String bankName) {
        // 1. 해당 부모의 자녀들 찾기
        List<FamilyConnection> connections = familyConnectionRepository
                .findByParentUserIdAndConnectionStatus(parentUserId, ConnectionStatus.APPROVED);

        if (connections.isEmpty()) {
            log.warn("부모 {}의 연결된 자녀가 없습니다.", parentUserId);
            return false;
        }

        boolean anySuccess = false;

        // 2. 각 자녀에게 경고 알림 발송
        for (FamilyConnection connection : connections) {
            Long childUserId = connection.getChild().getUserId();

            try {
                // 경고 메시지 생성
                FcmNotificationRequest notification = fcmMessageTemplate
                        .createFraudWarningNotification(childUserId, accountNumber, bankName);

                // 알림 발송
                boolean success = sendNotificationToUser(
                        notification.getUserId(),
                        notification.getTitle(),
                        notification.getMessage()
                );

                if (success) {
                    anySuccess = true;
                    log.info("사기계좌 경고 알림 발송 성공: parentUserId={}, childUserId={}",
                            parentUserId, childUserId);
                }

            } catch (Exception e) {
                log.error("사기계좌 경고 알림 발송 실패: parentUserId={}, childUserId={}, error={}",
                        parentUserId, childUserId, e.getMessage());
            }
        }

        return anySuccess;
    }

    /**
     * 토큰 비활성화
     */
    @Transactional
    public void deactivateToken(FcmToken fcmToken) {
        fcmToken.setIsActive(false);
        fcmTokenRepository.save(fcmToken);
        log.info("FCM 토큰 비활성화: {}", maskToken(fcmToken.getFcmToken()));
    }

    /**
     * 유효하지 않은 토큰 오류인지 확인
     */
    private boolean isInvalidTokenError(Exception e) {
        if (e instanceof FirebaseMessagingException) {
            FirebaseMessagingException fme = (FirebaseMessagingException) e;
            MessagingErrorCode errorCode = fme.getMessagingErrorCode();

            return errorCode == MessagingErrorCode.INVALID_ARGUMENT ||
                    errorCode == MessagingErrorCode.UNREGISTERED;
        }
        return false;
    }

    /**
     * 토큰 마스킹 (로그용)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "***";
    }
}