package com.grandma.ansimbank.fcm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmNotificationRequest {
    private Long userId;        // 수신자 사용자 ID
    private String title;       // 알림 제목
    private String message;     // 알림 내용
}
