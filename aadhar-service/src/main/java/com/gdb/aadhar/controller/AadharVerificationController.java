package com.gdb.aadhar.controller;

import com.gdb.aadhar.dto.AadharVerificationRequest;
import com.gdb.aadhar.dto.AadharVerificationResponse;
import com.gdb.aadhar.dto.ErrorResponse;
import com.gdb.aadhar.service.AadharVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Aadhar number verification.
 * Simulates the UIDAI verification system for the GDB ecosystem.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Aadhar Verification", description = "Aadhar number verification endpoints for KYC compliance")
public class AadharVerificationController {

    private final AadharVerificationService verificationService;

    /**
     * FR-1: Verify Aadhar Number (POST)
     * Primary endpoint used by Account Service during savings account creation.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify Aadhar number (POST)", description = "Verify an Aadhar number against the UIDAI database. Used by Account Service during savings account creation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification completed (check is_valid field)"),
            @ApiResponse(responseCode = "400", description = "Invalid Aadhar number format")
    })
    public ResponseEntity<?> verifyAadharPost(@Valid @RequestBody AadharVerificationRequest request) {
        log.info("POST /api/v1/verify - Received verification request");

        // Validate format
        String validationError = verificationService.validateFormat(request.getAadharNumber());
        if (validationError != null) {
            log.warn("Format validation failed: {}", validationError);
            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error("invalid_format")
                            .message(validationError)
                            .build());
        }

        // Verify against valid numbers
        AadharVerificationResponse response = verificationService.verify(request.getAadharNumber());
        return ResponseEntity.ok(response);
    }

    /**
     * FR-2: Verify Aadhar Number (GET)
     * Convenience endpoint for frontend applications.
     */
    @GetMapping("/verify/{aadharNumber}")
    @Operation(summary = "Verify Aadhar number (GET)", description = "Verify an Aadhar number using a GET request. Convenience endpoint for frontend use.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification completed (check is_valid field)"),
            @ApiResponse(responseCode = "400", description = "Invalid Aadhar number format")
    })
    public ResponseEntity<?> verifyAadharGet(
            @Parameter(description = "12-digit Aadhar number", required = true) @PathVariable String aadharNumber) {
        log.info("GET /api/v1/verify/{} - Received verification request", aadharNumber);

        // Validate format
        String validationError = verificationService.validateFormat(aadharNumber);
        if (validationError != null) {
            log.warn("Format validation failed: {}", validationError);

            // Determine specific error code
            String errorCode;
            if (!aadharNumber.matches("\\d+")) {
                errorCode = "non_numeric";
            } else if (aadharNumber.length() != 12) {
                errorCode = "not_12_digits";
            } else {
                errorCode = "invalid_format";
            }

            return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                            .error(errorCode)
                            .message(validationError)
                            .build());
        }

        // Verify against valid numbers
        AadharVerificationResponse response = verificationService.verify(aadharNumber);
        return ResponseEntity.ok(response);
    }

    /**
     * FR-3: Get Valid Aadhar Numbers
     * Returns the list of valid Aadhar numbers for testing purposes.
     */
    @GetMapping("/valid-numbers")
    @Operation(summary = "Get valid Aadhar numbers", description = "Retrieve the list of all valid Aadhar numbers for testing purposes.")
    @ApiResponse(responseCode = "200", description = "Valid numbers retrieved successfully")
    public ResponseEntity<Map<String, Object>> getValidNumbers() {
        log.info("GET /api/v1/valid-numbers - Retrieving valid Aadhar numbers");

        List<String> validNumbers = verificationService.getValidAadharNumbers();

        Map<String, Object> response = Map.of(
                "valid_aadhar_numbers", validNumbers,
                "count", validNumbers.size());

        return ResponseEntity.ok(response);
    }
}
