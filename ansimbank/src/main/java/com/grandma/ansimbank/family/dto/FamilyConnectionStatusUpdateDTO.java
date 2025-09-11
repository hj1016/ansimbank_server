package com.grandma.ansimbank.family.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConnectionStatusUpdateDTO {
    private Long userId;
    @NotNull(message = "연결 상태는 필수입니다")
    private String status;
    private String reason;
}