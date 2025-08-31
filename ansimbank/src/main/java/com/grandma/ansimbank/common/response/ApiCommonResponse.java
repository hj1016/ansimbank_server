package com.grandma.ansimbank.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiCommonResponse<T> {
    private String status; // success | fail | error

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String code;

    public static <T> ApiCommonResponse<T> success(T data) {
        return ApiCommonResponse.<T>builder()
            .status(ApiStatus.SUCCESS.getStatus())
            .data(data)
            .build();
    }

    // Bean Validation 실패 등 필드 오류 맵을 담아서 내려줄 때 사용
    public static ApiCommonResponse<?> fail(Object errors) {
        return ApiCommonResponse.builder()
            .status(ApiStatus.FAIL.getStatus())
            .data(errors)
            .build();
    }

    // 시스템/도메인 에러를 공통 포맷으로 내려야 할 때(선택)
    public static ApiCommonResponse<?> error(String message, String code) {
        return ApiCommonResponse.builder()
            .status(ApiStatus.ERROR.getStatus())
            .message(message)
            .code(code)
            .build();
    }
}
