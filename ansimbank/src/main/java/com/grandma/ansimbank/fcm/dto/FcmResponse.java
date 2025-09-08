package com.grandma.ansimbank.fcm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmResponse {

    private boolean success;        // 성공 여부
    private String message;         // 응답 메시지
    private String errorCode;       // 오류 코드 (실패시)

    // 성공 응답 생성
    public static FcmResponse success(String message) {
        return FcmResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    // 실패 응답 생성
    public static FcmResponse failure(String message, String errorCode) {
        return FcmResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}