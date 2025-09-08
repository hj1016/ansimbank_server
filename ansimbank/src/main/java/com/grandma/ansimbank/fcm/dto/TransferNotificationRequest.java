package com.grandma.ansimbank.fcm.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferNotificationRequest {

    private Long receiverUserId;    // 수신자 사용자 ID
    private String senderName;      // 송금인 이름
    private Long amount;            // 송금 금액
    private String memo;            // 송금 메모 (선택)
}
