package com.grandma.ansimbank.preset.dto;

import com.grandma.ansimbank.preset.OneClickPreset;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresetResponseDTO {
    
    private Long presetId;
    private Long userId;
    private String presetName;
    private String receiverAccount;
    private String receiverName;
    private String receiverBank;
    private BigDecimal defaultAmount;
    private String buttonColor;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PresetResponseDTO from(OneClickPreset preset) {
        return PresetResponseDTO.builder()
                .presetId(preset.getPresetId())
                .userId(preset.getUser().getUserId())
                .presetName(preset.getPresetName())
                .receiverAccount(preset.getReceiverAccount())
                .receiverName(preset.getReceiverName())
                .receiverBank(preset.getReceiverBank())
                .defaultAmount(preset.getDefaultAmount())
                .buttonColor(preset.getButtonColor())
                .displayOrder(preset.getDisplayOrder())
                .createdAt(preset.getCreatedAt())
                .updatedAt(preset.getUpdatedAt())
                .build();
    }
}