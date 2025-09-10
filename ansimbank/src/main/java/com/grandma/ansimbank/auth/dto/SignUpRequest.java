package com.grandma.ansimbank.auth.dto;

import com.grandma.ansimbank.common.constants.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank(message = "사용자명은 필수입니다")
    private String username;

    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이름은 필수입니다")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;

    @NotNull(message = "계정 유형을 선택해주세요")
    private UserType userType;

    // 가족 연동 요청할 아이디 (선택사항)
    private String familyUsername;
}