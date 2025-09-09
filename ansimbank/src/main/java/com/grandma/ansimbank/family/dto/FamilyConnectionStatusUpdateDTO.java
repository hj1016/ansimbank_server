package com.grandma.ansimbank.family.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConnectionStatusUpdateDTO {
    
    // TODO: JWT 토큰 도입 시 이 필드 제거 예정
    // JWT에서 자동으로 사용자 정보를 추출하므로 요청 DTO에서는 불필요
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId; // 승인/거부하는 사용자 ID
    
    @NotNull(message = "연결 상태는 필수입니다")
    private String status; // "APPROVED" 또는 "REJECTED"
    
    private String reason; // 거부 사유 (선택사항)
}