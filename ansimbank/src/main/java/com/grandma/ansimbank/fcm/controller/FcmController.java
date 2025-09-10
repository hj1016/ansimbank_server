package com.grandma.ansimbank.fcm.controller;

import com.grandma.ansimbank.common.response.ApiCommonResponse;
import com.grandma.ansimbank.fcm.dto.*;
import com.grandma.ansimbank.fcm.service.FcmService;
import com.grandma.ansimbank.fcm.template.FcmMessageTemplate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
public class FcmController {

    /*
    1. FCM 토큰 등록 api: registerToken
    2. 위임송금 알림 api: delegation
    3. 사기계좌 알림 api: fraud-warning
    * */
    private final FcmService fcmService;


    /**
     * FCM 토큰 등록 API
     * 앱에서 FCM 토큰을 등록할 때 사용
     */
    @PostMapping("/token")
    public ResponseEntity<ApiCommonResponse<?>> registerToken(  // ← <?> 로 변경
                                                                @Valid @RequestBody FcmTokenRequest request) {

        try {
            log.info("FCM 토큰 등록 요청: userId={}", request.getUserId());

            fcmService.registerToken(request.getUserId(), request.getFcmToken());

            FcmResponse response = FcmResponse.success("FCM 토큰이 성공적으로 등록되었습니다.");
            return ResponseEntity.ok(ApiCommonResponse.success(response));

        } catch (Exception e) {
            log.error("FCM 토큰 등록 실패: userId={}, error={}",
                    request.getUserId(), e.getMessage());

            return ResponseEntity.badRequest()
                    .body(ApiCommonResponse.error("FCM 토큰 등록에 실패했습니다.", "FCM_TOKEN_REGISTER_FAILED"));
        }
    }

    /*
    위임업무 알림 발송 API
  자식이 부모 계좌 이용 시 부모에게 알림
  */
    @PostMapping("/delegation")
    public ResponseEntity<ApiCommonResponse<?>> sendDelegationNotification(
            @Valid @RequestBody DelegationNotificationRequest request) {

        try {
            log.info("위임업무 알림 발송 요청: childUserId={}, parentUserId={}, action={}",
                    request.getChildUserId(), request.getParentUserId(), request.getActionType());

            boolean success = fcmService.sendDelegationNotification(request);

            if (success) {
                FcmResponse response = FcmResponse.success("위임업무 알림이 성공적으로 발송되었습니다.");
                return ResponseEntity.ok(ApiCommonResponse.success(response));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiCommonResponse.fail("알림 발송에 실패했습니다."));
            }

        } catch (IllegalArgumentException e) {
            log.error("위임업무 알림 발송 실패 - 잘못된 요청: childUserId={}, error={}",
                    request.getChildUserId(), e.getMessage());

            return ResponseEntity.badRequest()
                    .body(ApiCommonResponse.fail(e.getMessage()));

        } catch (Exception e) {
            log.error("위임업무 알림 발송 실패 - 서버 오류: childUserId={}, error={}",
                    request.getChildUserId(), e.getMessage());

            return ResponseEntity.internalServerError()
                    .body(ApiCommonResponse.error("알림 발송 중 오류가 발생했습니다.", "FCM_DELEGATION_ERROR"));
        }
    }

    /**
     * 사기 계좌 경고 알림 API
     * 부모가 사기계좌 송금 시도 시 자식에게 알림
     */
    @PostMapping("/fraud-warning")
    public ResponseEntity<ApiCommonResponse<?>> sendFraudWarning(
            @RequestParam Long parentUserId,  // 부모 ID
            @RequestParam String accountNumber,
            @RequestParam String bankName) {

        try {
            log.info("사기 계좌 경고 알림: parentUserId={}, account={}", parentUserId, accountNumber);

            boolean success = fcmService.sendFraudWarningToChildren(parentUserId, accountNumber, bankName);

            if (success) {
                FcmResponse response = FcmResponse.success("사기 계좌 경고 알림이 발송되었습니다.");
                return ResponseEntity.ok(ApiCommonResponse.success(response));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiCommonResponse.fail("알림 발송에 실패했습니다."));
            }

        } catch (Exception e) {
            log.error("사기 계좌 경고 알림 실패: parentUserId={}, error={}", parentUserId, e.getMessage());

            return ResponseEntity.internalServerError()
                    .body(ApiCommonResponse.error("사기 계좌 경고 알림 발송 중 오류가 발생했습니다.", "FCM_FRAUD_WARNING_ERROR"));
        }
    }
    }


