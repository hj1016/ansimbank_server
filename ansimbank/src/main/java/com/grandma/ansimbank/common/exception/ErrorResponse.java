package com.grandma.ansimbank.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.grandma.ansimbank.common.constants.ErrorCode;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    private int status;     // HTTP status code
    private String error;   // HTTP status name
    private String code;    // service error code (optional)
    private String message; // error message
    private String path;    // request path

    public static ErrorResponse of(ErrorCode ec, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ec.getStatus().value())
            .error(ec.getStatus().name())
            .code(ec.getCode())
            .message(ec.getMsg())
            .path(path)
            .build();
    }

    public static ErrorResponse of(ErrorCode ec, String customMessage, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(ec.getStatus().value())
            .error(ec.getStatus().name())
            .code(ec.getCode())
            .message(customMessage)
            .path(path)
            .build();
    }

    public static ErrorResponse from(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.name())
            .message(message)
            .path(path)
            .build();
    }
}
