package com.grandma.ansimbank.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDTO {
    
    // TODO: JWT 토큰 도입 시 이 필드 제거 - JWT에서 사용자 ID 추출
    @NotNull(message = "송금인 ID는 필수입니다")
    private Long senderId;
    
    @NotBlank(message = "송금 계좌는 필수입니다")
    private String senderAccount;
    
    @NotBlank(message = "수취 계좌는 필수입니다")
    private String receiverAccount;
    
    @NotBlank(message = "수취인명은 필수입니다")
    private String receiverName;
    
    @NotBlank(message = "수취은행은 필수입니다")
    private String receiverBank;
    
    @NotNull(message = "송금 금액은 필수입니다")
    @Positive(message = "송금 금액은 0보다 커야 합니다")
    private BigDecimal amount;
    
    private String memo;
    
    private Long delegationId;
    
    private Long presetId;
}