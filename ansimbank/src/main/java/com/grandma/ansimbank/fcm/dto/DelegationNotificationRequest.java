package com.grandma.ansimbank.fcm.dto;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DelegationNotificationRequest {

    private Long childUserId;        // 실제 송금한 자식

    private Long parentUserId;       // 부모 (옵셔널, 없으면 자동으로 찾음)

    private String actionType;       // 송금

    private Long amount;             // 송금액
    private String targetAccount;    // 수신계좌
}