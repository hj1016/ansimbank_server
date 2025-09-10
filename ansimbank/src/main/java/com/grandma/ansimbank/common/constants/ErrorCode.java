package com.grandma.ansimbank.common.constants;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR("E40001", "요청값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("E40100", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("E40300", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("E40400", "리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("E40900", "상태 충돌이 발생했습니다.", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("E50000", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_COMMAND("V001", "죄송합니다, 무슨 말씀이신지 잘 모르겠어요.", HttpStatus.BAD_REQUEST),
    TARGET_NOT_FOUND("V002", "누구에게 송금할지 말씀해주세요.", HttpStatus.BAD_REQUEST),
    AMOUNT_NOT_FOUND("V003", "송금할 금액을 말씀해주세요.", HttpStatus.BAD_REQUEST),

    // ===================================
    // 송금 및 계좌 관리 에러 코드
    // 400 Bad Request - 송금 및 계좌 유효성 검증
    INVALID_ACCOUNT_NUMBER("E40002", "유효하지 않은 계좌번호입니다.", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE("E40003", "계좌 잔액이 부족합니다.", HttpStatus.BAD_REQUEST),
    TRANSFER_LIMIT_EXCEEDED("E40004", "송금 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    SAME_ACCOUNT_TRANSFER("E40005", "동일한 계좌로는 송금할 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_TRANSFER_AMOUNT("E40006", "유효하지 않은 송금 금액입니다.", HttpStatus.BAD_REQUEST),
    PRESET_LIMIT_EXCEEDED("E40007", "원클릭 프리셋 등록 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    DELEGATION_EXPIRED("E40008", "위임장이 만료되었습니다.", HttpStatus.BAD_REQUEST),
    DELEGATION_LIMIT_EXCEEDED("E40009", "위임장 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    ACCOUNT_LINK_LIMIT_EXCEEDED("E40010", "계좌 연동 한도를 초과했습니다.", HttpStatus.BAD_REQUEST),
    PRIMARY_ACCOUNT_REQUIRED("E40011", "주계좌 설정이 필요합니다.", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_DATA("E40012", "요청 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    
    // 403 Forbidden - 송금 및 계좌 권한
    UNAUTHORIZED_ACCOUNT("E40301", "계좌 사용 권한이 없습니다.", HttpStatus.FORBIDDEN),
    DELEGATION_NOT_APPROVED("E40302", "승인되지 않은 위임장입니다.", HttpStatus.FORBIDDEN),
    
    // 404 Not Found - 송금 및 계좌 리소스
    USER_NOT_FOUND("E40401", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND("E40402", "계좌를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PRESET_NOT_FOUND("E40403", "원클릭 프리셋을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DELEGATION_NOT_FOUND("E40404", "위임장을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 409 Conflict - 송금 및 계좌 중복
    DUPLICATE_PRESET_NAME("E40901", "이미 존재하는 프리셋 이름입니다.", HttpStatus.CONFLICT),
    ACCOUNT_ALREADY_LINKED("E40902", "이미 연동된 계좌입니다.", HttpStatus.CONFLICT),
    
    // 500 Internal Server Error - 송금 및 계좌 시스템
    EXTERNAL_API_ERROR("E50001", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // ===================================
    // 가족 연동 에러 코드
    // 400 Bad Request - 가족 연동 유효성 검증
    INVALID_FAMILY_CONNECTION_REQUEST("E40013", "잘못된 가족 연동 요청입니다.", HttpStatus.BAD_REQUEST),
    INVALID_CONNECTION_STATUS("E40014", "유효하지 않은 연결 상태입니다.", HttpStatus.BAD_REQUEST),
    
    // 403 Forbidden - 가족 연동 권한
    UNAUTHORIZED_FAMILY_CONNECTION("E40303", "가족 연동 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // 404 Not Found - 가족 연동 리소스
    FAMILY_CONNECTION_NOT_FOUND("E40405", "가족 연동 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 409 Conflict - 가족 연동 중복/상태
    FAMILY_CONNECTION_ALREADY_EXISTS("E40903", "이미 연동된 가족입니다.", HttpStatus.CONFLICT),
    FAMILY_CONNECTION_PENDING("E40904", "이미 요청 중인 가족 연동이 있습니다.", HttpStatus.CONFLICT),
    FAMILY_CONNECTION_ALREADY_PROCESSED("E40905", "이미 처리된 가족 연동 요청입니다.", HttpStatus.CONFLICT);
    // ===================================

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
