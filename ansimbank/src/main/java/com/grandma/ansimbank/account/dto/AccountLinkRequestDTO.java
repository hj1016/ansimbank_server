package com.grandma.ansimbank.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLinkRequestDTO {
    
    // TODO: JWT 토큰 도입 시 이 필드 제거 - JWT에서 사용자 ID 추출
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotBlank(message = "연동 방식은 필수입니다")
    private String linkType; // "MANUAL" 또는 "CODEF"
    
    @NotBlank(message = "은행 코드는 필수입니다")
    private String bankCode;
    
    @NotBlank(message = "은행명은 필수입니다")
    private String bankName;
    
    // MANUAL 방식에서만 필수
    private String accountNumber;
    
    // MANUAL 방식에서만 필수  
    private String accountHolder;
    
    // CODEF 방식에서만 필수
    private String bankUserId;
    
    // CODEF 방식에서만 필수
    private String bankUserPassword;
    
    @Builder.Default
    private Boolean isPrimary = false;
}