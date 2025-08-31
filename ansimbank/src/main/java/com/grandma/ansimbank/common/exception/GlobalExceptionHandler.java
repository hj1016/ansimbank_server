package com.grandma.ansimbank.common.exception;

import com.grandma.ansimbank.common.constants.ErrorCode;
import com.grandma.ansimbank.common.response.ApiCommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.validation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * @Valid 본문 검증 실패 → 400 + ApiCommonResponse.fail({field: message})
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiCommonResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpServletRequest req) {
        Map<String, String> errors = new HashMap<>();
        BindingResult br = ex.getBindingResult();
        for (ObjectError oe : br.getAllErrors()) {
            if (oe instanceof FieldError fe) errors.put(fe.getField(), fe.getDefaultMessage());
            else errors.put(oe.getObjectName(), oe.getDefaultMessage());
        }
        log.warn("Validation failed: {} {}", req.getRequestURI(), errors);
        return ResponseEntity.badRequest().body(ApiCommonResponse.fail(errors));
    }

    /**
     * 비즈니스/도메인 예외 → ErrorResponse + 지정된 HTTP 상태
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException ex, HttpServletRequest req) {
        ErrorResponse body = ErrorResponse.of(ex.getErrorCode(), req.getRequestURI());
        log.warn("Business error {} at {}: {}", ex.getErrorCode().getCode(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(body);
    }

    /**
     * 잘못된 요청 흐름 → 400
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        log.warn("Bad request at {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.badRequest().body(ErrorResponse.from(HttpStatus.BAD_REQUEST, ex.getMessage(), req.getRequestURI()));
    }

    /**
     * 그 외 모든 예외 → 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, req.getRequestURI()));
    }
}
