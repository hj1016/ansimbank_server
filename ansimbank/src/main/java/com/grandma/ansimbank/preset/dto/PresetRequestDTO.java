package com.grandma.ansimbank.preset.dto;

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
public class PresetRequestDTO {
    
    // TODO: JWT 토큰 도입 시 이 필드 제거 - JWT에서 사용자 ID 추출
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "프리셋 이름은 필수입니다")
    private String presetName;
    
    @NotBlank(message = "수취 계좌는 필수입니다")
    private String receiverAccount;
    
    @NotBlank(message = "수취인명은 필수입니다")
    private String receiverName;
    
    @NotBlank(message = "수취은행은 필수입니다")
    private String receiverBank;
    
    @Positive(message = "기본 송금액은 0보다 커야 합니다")
    private BigDecimal defaultAmount;
    
    private String buttonColor;
    
    private Integer displayOrder;
}