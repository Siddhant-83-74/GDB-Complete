package com.gdb.creditcards.exception;

import com.gdb.creditcards.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler producing consistent error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CreditCardException.class)
    public ResponseEntity<ErrorResponse> handleCreditCardException(CreditCardException ex) {
        log.error("Credit card exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse error = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        HttpStatus status = switch (ex.getErrorCode()) {
            case "CARD_NOT_FOUND", "TRANSACTION_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CARD_NOT_ACTIVE", "CARD_BLOCKED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "LIMIT_EXCEEDED", "SERVICE_LIMIT_EXCEEDED", "INTERNATIONAL_DISABLED",
                    "INVALID_CARD_TYPE" -> HttpStatus.BAD_REQUEST;
            case "TRANSACTION_ON_HOLD" -> HttpStatus.ACCEPTED;
            default -> HttpStatus.BAD_REQUEST;
        };
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse error = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        if ("ACCESS_DENIED".equals(ex.getMessage())) {
            log.warn("Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                    .errorCode("ACCESS_DENIED")
                    .message("You do not have permission to perform this action")
                    .timestamp(LocalDateTime.now())
                    .build());
        }
        log.error("Runtime exception: ", ex);
        return new ResponseEntity<>(ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
            jakarta.servlet.http.HttpServletRequest request) throws Exception {
        String path = request.getRequestURI();
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/api/v1/api-docs")) {
            throw ex;
        }
        if (ex.getClass().getName().contains("NoResourceFoundException")) {
            throw ex;
        }
        log.error("Unexpected error occurred", ex);
        ErrorResponse error = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
