package com.gdb.creditcards.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Credit card aggregate root.
 *
 * Security note (requirement f): the full PAN is never stored in plaintext.
 * {@code cardNumberEncrypted} holds the AES-encrypted PAN, {@code cardLast4}
 * and {@code cardBin} are kept for display/vendor logic, and the CVV is only
 * ever persisted as a bcrypt hash ({@code cvvHash}).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {
    private String id;
    private Long userId;
    private String cardHolderName;       // b.8 mandatory
    private String mobileNumber;         // b.5 mapped to the card

    // PAN handling (b.1, b.10, b.11, b.12, f)
    private String cardNumberEncrypted;  // AES-encrypted full PAN
    private String cardBin;              // first 6 digits (vendor/network)
    private String cardLast4;            // last 4 for masking
    private String vendor;               // VISA / MASTERCARD / RUPAY

    private String category;             // SILVER / GOLD / PLATINUM (b.13)
    private String cvvHash;              // b.4 bcrypt-hashed CVV
    private LocalDate expiryDate;        // b.2

    private BigDecimal creditLimit;      // b.3 / b.13
    private BigDecimal availableCredit;
    private BigDecimal outstandingAmount;

    private Long linkedAccountNumber;    // b.14 optional account mapping
    private Boolean internationalEnabled; // b.16
    private String status;               // ACTIVE / INACTIVE / BLOCKED (b.9)

    // Admin-facing application metadata (officer-raised applications)
    private String consentSource;        // comma-separated: PHYSICAL_FORM,DIGITAL_SIGNATURE,VERBAL_OTP
    private Boolean otpVerified;         // applicant authorised the action via OTP
    private String leadSource;           // BRANCH / COLD_CALL / DIGITAL_CAMPAIGN
    private String sourcingBranchCode;   // physical branch/hub credited with the application
    private String kycDocumentName;      // uploaded KYC scan reference
    private String incomeDocumentName;   // uploaded income proof scan reference

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
