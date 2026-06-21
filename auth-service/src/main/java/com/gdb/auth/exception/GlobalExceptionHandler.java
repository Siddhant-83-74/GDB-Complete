package com.gdb.auth.exception;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Data
    @Builder
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private String status;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "INTERNAL_ERROR";

        if ("AUTHENTICATION_FAILED".equals(message)) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "AUTHENTICATION_FAILED";
            message = "Invalid login ID or password";
        } else if ("USER_LOCKED".equals(message)) {
            status = HttpStatus.FORBIDDEN;
            errorCode = "USER_LOCKED";
            message = "User account is inactive or locked";
        } else if ("REFRESH_TOKEN_INVALID".equals(message)) {
            status = HttpStatus.UNAUTHORIZED;
            errorCode = "REFRESH_TOKEN_INVALID";
            message = "Refresh token is invalid or expired";
        }

        ErrorResponse error = ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .status("error")
                .build();

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed: " + errors)
                .status("error")
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .status("error")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
