package com.grandma.ansimbank.common.constants;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR("E40001", "요청값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("E40100", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E40300", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("E40400", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("E40900", "상태 충돌이 발생했습니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("E50000", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String msg;
    private final HttpStatus status;

    ErrorCode(String code, String msg, HttpStatus status) {
        this.code = code;
        this.msg = msg;
        this.status = status;
    }

    public String getCode() { return code; }
    public String getMsg() { return msg; }
    public HttpStatus getStatus() { return status; }
}
