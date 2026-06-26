package com.gdb.creditcards.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Application payload for a new credit card.
 * Bean Validation here enforces the structural P1 rules; cross-field and
 * temporal rules (e.g. b.2 expiry >= 3 years) are enforced in the service /
 * CardValidator where "now" is available.
 */
@Data
public class ApplyCardRequest {

    // b.8 Name is mandatory
    @NotBlank(message = "Card holder name is mandatory")
    @Size(min = 2, max = 255)
    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @NotNull(message = "User id is required")
    @JsonProperty("user_id")
    private Long userId;

    // b.5 Mobile number must be mapped to the card
    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits")
    @JsonProperty("mobile_number")
    private String mobileNumber;

    // b.10 Type must be one of the supported networks
    @NotBlank
    @Pattern(regexp = "VISA|MASTERCARD|RUPAY", message = "Vendor must be VISA, MASTERCARD or RUPAY")
    private String vendor;

    // b.13 Category drives the applicable credit limit
    @NotBlank
    @Pattern(regexp = "SILVER|GOLD|PLATINUM", message = "Category must be SILVER, GOLD or PLATINUM")
    private String category;

    // b.2 Expiry cannot be less than 3 years from today (validated in service)
    @NotNull(message = "Expiry date is required")
    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    // b.4 CVV must be exactly 3 digits
    @NotBlank
    @Pattern(regexp = "^\\d{3}$", message = "CVV must be 3 digits")
    private String cvv;

    // b.14 Optional account to map the card to at creation time
    @JsonProperty("linked_account_number")
    private Long linkedAccountNumber;
}
