package com.gdb.payment.exception;

import com.gdb.payment.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * Global exception handler for the Payment Gateway Service.
 * Maps validation errors to HTTP 422 and internal errors to HTTP 500.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation errors (e.g., @NotNull, @DecimalMin).
     * Returns HTTP 422 Unprocessable Entity per requirements.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn("Validation error: {}", message);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                PaymentResponse.builder()
                        .success(false)
                        .transactionId(null)
                        .message(message)
                        .gatewayRefId(UUID.randomUUID().toString())
                        .build());
    }

    /**
     * Handles malformed JSON or invalid enum values (e.g., invalid payment mode).
     * Returns HTTP 422 Unprocessable Entity.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<PaymentResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Invalid request body";
        if (ex.getMessage() != null && ex.getMessage().contains("PaymentMode")) {
            message = "Invalid payment mode. Supported modes: NEFT, RTGS, IMPS, UPI";
        }

        log.warn("Message not readable: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                PaymentResponse.builder()
                        .success(false)
                        .transactionId(null)
                        .message(message)
                        .gatewayRefId(UUID.randomUUID().toString())
                        .build());
    }

    /**
     * Catches all unexpected exceptions.
     * Returns HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                PaymentResponse.builder()
                        .success(false)
                        .transactionId(null)
                        .message("Internal server error")
                        .gatewayRefId(UUID.randomUUID().toString())
                        .build());
    }
}
