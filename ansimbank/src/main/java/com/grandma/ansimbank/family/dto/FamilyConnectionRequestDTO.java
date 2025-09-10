package com.grandma.ansimbank.family.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConnectionRequestDTO {
    
    // TODO: JWT 토큰 도입 시 이 필드들 제거 예정
    // JWT에서 자동으로 사용자 정보를 추출하므로 요청 DTO에서는 불필요
    @NotNull(message = "요청자 ID는 필수입니다")
    private Long requesterId; // 부모 또는 자녀 ID (요청자)
    
    @NotNull(message = "대상자 전화번호는 필수입니다")
    private String targetPhoneNumber; // 연동할 상대방 전화번호
    
    private String message; // 연동 요청 메시지
}