package com.grandma.ansimbank.family.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FamilyConnectionRequestDTO {
    
    // JWT에서 사용자 정보를 추출하므로 클라이언트에서 제공하지 않아도 됨
    private Long requesterId; // 부모 또는 자녀 ID (요청자)
    
    @NotNull(message = "대상자 전화번호는 필수입니다")
    private String targetPhoneNumber; // 연동할 상대방 전화번호
    
    private String relationshipType; // 관계 유형 (선택사항)
    
    private String message; // 연동 요청 메시지
}