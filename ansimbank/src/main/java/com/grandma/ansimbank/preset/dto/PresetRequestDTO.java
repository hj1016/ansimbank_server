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
    
    @NotBlank(message = "프리셋 이름은 필수입니다")
    private String presetName;
    
    @NotBlank(message = "수취 계좌는 필수입니다")
    private String receiverAccount;
    
    @NotBlank(message = "수취인명은 필수입니다")
    private String receiverName;
    
    @NotBlank(message = "수취은행은 필수입니다")
    private String receiverBank;
    
    private BigDecimal defaultAmount;
    
    private String buttonColor;
    
    private Integer displayOrder;
}