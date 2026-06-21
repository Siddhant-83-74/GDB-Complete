package com.gdb.company.controller;

import com.gdb.company.dto.CompanyVerificationRequest;
import com.gdb.company.dto.CompanyVerificationResponse;
import com.gdb.company.service.CompanyVerificationService;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
@Tag(name = "Company Verification (CRV)", description = "Verify CIN with Ministry of Corporate Affairs (MCA)")
public class CompanyVerificationController {

        private final CompanyVerificationService verificationService;

        /**
         * Verify a Company Identification Number (CIN).
         * Used by Account Service during Current Account creation.
         */
        @PostMapping("/verify")
        @Operation(summary = "Verify CIN (POST)", description = "Verify a 21-character Company Identification Number (CIN).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Verification successful (check is_valid field)"),
                        @ApiResponse(responseCode = "400", description = "Invalid CIN format")
        })
        public ResponseEntity<CompanyVerificationResponse> verifyCompany(
                        @Valid @RequestBody CompanyVerificationRequest request) {
                log.info("Received verification request for CIN: {}", request.getRegistrationNumber());
                return ResponseEntity.ok(verificationService.verify(request.getRegistrationNumber()));
        }

        /**
         * Verify a Company Identification Number (GET).
         * Convenience endpoint.
         */
        @GetMapping("/verify/{registrationNumber}")
        @Operation(summary = "Verify CIN (GET)", description = "Verify a CIN using a GET request.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Verification successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid CIN format")
        })
        public ResponseEntity<?> verifyCompanyGet(
                        @Parameter(description = "21-character CIN", required = true) @PathVariable String registrationNumber) {
                // Basic format check handled by service or return invalid response?
                // Let's rely on service logic which returns INVALID if not in list.
                // But Controller should validate format first?
                // Since @PathVariable validation is tricky without @Validated on class,
                // I will let service/logic allow it, but return INVALID if format is clearly
                // wrong?
                // The requirements say 400 for invalid format.
                // I'll add manual regex check here for consistency with POST endpoint.

                if (!registrationNumber.matches("^[UL]\\d{5}[A-Z]{2}\\d{4}[A-Z]{3}\\d{6}$")) {
                        return ResponseEntity.badRequest().body(
                                        com.gdb.company.dto.ErrorResponse.builder()
                                                        .error("invalid_format")
                                                        .message("Invalid Company Identification Number (CIN) format")
                                                        .build());
                }

                return ResponseEntity.ok(verificationService.verify(registrationNumber));
        }

        /**
         * Get list of valid CINs for testing.
         */
        @GetMapping("/valid-companies")
        @Operation(summary = "Get valid CINs", description = "Retrieve list of valid Company numbers for testing.")
        public ResponseEntity<Map<String, Object>> getValidCompanies() {
                return ResponseEntity.ok(Map.of(
                                "valid_companies", verificationService.getValidCompanies(),
                                "count", verificationService.getValidCompanies().size()));
        }
}
