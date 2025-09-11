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
    
    // JWT에서 사용자 ID를 추출하므로 클라이언트에서 제공하지 않아도 됨
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