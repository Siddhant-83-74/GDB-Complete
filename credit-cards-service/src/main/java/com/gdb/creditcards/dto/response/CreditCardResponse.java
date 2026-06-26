package com.gdb.creditcards.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Card view returned to clients. The PAN is ALWAYS masked here (b.12) — only
 * the last 4 digits are exposed, and the CVV is never returned.
 */
@Data
@Builder
public class CreditCardResponse {
    private String id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    // b.12 masked PAN, e.g. "**** **** **** 1234"
    @JsonProperty("card_number")
    private String maskedCardNumber;

    private String vendor;
    private String category;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("mobile_number")
    private String mobileNumber;

    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    @JsonProperty("available_credit")
    private BigDecimal availableCredit;

    @JsonProperty("outstanding_amount")
    private BigDecimal outstandingAmount;

    @JsonProperty("linked_account_number")
    private Long linkedAccountNumber;

    @JsonProperty("international_enabled")
    private Boolean internationalEnabled;

    private String status;

    @JsonProperty("service_limits")
    private List<ServiceLimitView> serviceLimits;

    @Data
    @Builder
    public static class ServiceLimitView {
        private String channel;
        @JsonProperty("per_txn_limit")
        private BigDecimal perTxnLimit;
    }
}
