package com.grandma.ansimbank.fcm.template;

import com.grandma.ansimbank.fcm.dto.DelegationNotificationRequest;
import com.grandma.ansimbank.fcm.dto.FcmNotificationRequest;
import com.grandma.ansimbank.fcm.dto.TransferNotificationRequest;
import com.grandma.ansimbank.fcm.entity.User;
import com.grandma.ansimbank.fcm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class FcmMessageTemplate {

    private final UserRepository userRepository;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);

    /**
     * 송금 알림 메시지 생성
     */
    public FcmNotificationRequest createTransferNotification(TransferNotificationRequest request) {
        String title = "💰 송금 도착";

        String message = String.format("%s님이 %s원을 보냈습니다.",
                request.getSenderName(),
                formatAmount(request.getAmount())
        );

        // 메모가 있으면 추가
        if (request.getMemo() != null && !request.getMemo().trim().isEmpty()) {
            message += "\n메모: " + request.getMemo();
        }

        return FcmNotificationRequest.builder()
                .userId(request.getReceiverUserId())
                .title(title)
                .message(message)
                .build();
    }

    /**
     * 위임업무 알림 메시지 생성
     */
    public FcmNotificationRequest createDelegationNotification(DelegationNotificationRequest request, Long targetUserId) {
        String title = "🔄 자녀 계좌 사용 알림";

        // 자식 사용자 정보 조회
        User childUser = userRepository.findById(request.getChildUserId())
                .orElseThrow(() -> new RuntimeException("자식 사용자를 찾을 수 없습니다: " + request.getChildUserId()));

        String message;

        // 업무 타입에 따른 메시지 생성
        if ("송금".equals(request.getActionType()) && request.getAmount() != null) {
            message = String.format("%s님이 회원님의 계좌로 %s원을 송금했습니다.",
                    childUser.getName(),
                    formatAmount(request.getAmount())
            );

            // 대상 계좌가 있으면 추가
            if (request.getTargetAccount() != null && !request.getTargetAccount().trim().isEmpty()) {
                message += "\n수신계좌: " + maskAccount(request.getTargetAccount());
            }
        } else {
            message = String.format("%s님이 회원님의 계좌로 %s 업무를 수행했습니다.",
                    childUser.getName(),
                    request.getActionType()
            );
        }

        return FcmNotificationRequest.builder()
                .userId(targetUserId)  // 부모에게 알림
                .title(title)
                .message(message)
                .build();
    }

    /**
     * 사기 계좌 경고 알림 메시지 생성
     */
    public FcmNotificationRequest createFraudWarningNotification(Long userId, String accountNumber, String bankName) {
        String title = "⚠️ 사기 계좌 경고";

        String message = String.format("부모님이 송금하려는 계좌가 사기 의심 계좌입니다.\n%s %s\n송금을 확인해주세요.",
                bankName,
                maskAccount(accountNumber)
        );

        return FcmNotificationRequest.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .build();
    }

    /**
     * 일반 알림 메시지 생성 (기타 용도)
     */
    public FcmNotificationRequest createGeneralNotification(Long userId, String title, String message) {
        return FcmNotificationRequest.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .build();
    }

    /**
     * 금액 포맷팅 (쉼표 추가)
     */
    private String formatAmount(Long amount) {
        if (amount == null) {
            return "0";
        }
        return numberFormat.format(amount);
    }

    /**
     * 계좌번호 마스킹 (보안)
     */
    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return "***";
        }

        // 앞 3자리와 뒤 3자리만 보여주고 나머지는 마스킹
        String prefix = accountNumber.substring(0, 3);
        String suffix = accountNumber.substring(accountNumber.length() - 3);

        return prefix + "***" + suffix;
    }
}