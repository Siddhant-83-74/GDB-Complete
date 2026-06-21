package com.gdb.transactions.exception;

import com.gdb.transactions.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import com.gdb.transactions.exception.custom.BusinessException;

/**
 * Global exception handler for the transactions service.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException ex) {
        log.error("Transaction exception: {}", ex.getMessage(), ex);

        HttpStatus status = getHttpStatusForErrorCode(ex.getErrorCode());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .status("error")
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation exception: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("VALIDATION_ERROR")
                .message("Input validation failed: " + errors.toString())
                .status("error")
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        if ("ACCESS_DENIED".equals(ex.getMessage())) {
            log.warn("Access denied: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.builder()
                    .errorCode("ACCESS_DENIED")
                    .message("You do not have permission to perform this action")
                    .status("error")
                    .build());
        }
        return handleGenericException(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected exception: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .status("error")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case "ACCOUNT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ACCOUNT_NOT_ACTIVE", "ACCOUNT_INACTIVE" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "INSUFFICIENT_FUNDS", "INSUFFICIENT_BALANCE" -> HttpStatus.BAD_REQUEST;
            case "INVALID_PIN" -> HttpStatus.UNAUTHORIZED;
            case "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .status("error")
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    private HttpStatus getHttpStatusForErrorCode(String errorCode) {
        return switch (errorCode) {
            case "ACCOUNT_NOT_FOUND", "TRANSACTION_LOG_NOT_FOUND", "TRANSFER_LIMIT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_PIN", "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "ACCOUNT_NOT_ACTIVE", "SOURCE_ACCOUNT_INACTIVE", "DESTINATION_ACCOUNT_INACTIVE",
                    "BOTH_ACCOUNTS_INACTIVE", "INSUFFICIENT_FUNDS", "INVALID_AMOUNT",
                    "SAME_ACCOUNT_TRANSFER", "TRANSFER_LIMIT_EXCEEDED", "DAILY_TRANSACTION_COUNT_EXCEEDED",
                    "INVALID_TRANSFER_MODE" ->
                HttpStatus.BAD_REQUEST;
            case "VALIDATION_ERROR" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "ACCOUNT_SERVICE_ERROR" -> HttpStatus.BAD_GATEWAY;
            case "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}