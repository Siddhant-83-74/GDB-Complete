package com.gdb.creditcards.constants;

/**
 * Global constants and error codes for the Credit Cards Service.
 */
public final class CreditCardConstants {

    public static final String API_V1 = "/api/v1/credit-cards";
    public static final String INTERNAL_API_V1 = "/api/v1/internal/credit-cards";

    // Card vendors (b.10)
    public static final String VENDOR_VISA = "VISA";
    public static final String VENDOR_MASTERCARD = "MASTERCARD";
    public static final String VENDOR_RUPAY = "RUPAY";

    // Card categories / tiers (b.13)
    public static final String CATEGORY_SILVER = "SILVER";
    public static final String CATEGORY_GOLD = "GOLD";
    public static final String CATEGORY_PLATINUM = "PLATINUM";

    // Card status (b.9)
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_BLOCKED = "BLOCKED";

    // Transaction status (b.6, b.7)
    public static final String TXN_APPROVED = "APPROVED";
    public static final String TXN_ON_HOLD = "ON_HOLD";
    public static final String TXN_DECLINED = "DECLINED";
    public static final String TXN_REVERSED = "REVERSED";

    // Transaction types
    public static final String TXN_PURCHASE = "PURCHASE";
    public static final String TXN_PAYMENT = "PAYMENT";
    public static final String TXN_REFUND = "REFUND";

    // Service-limit channels (b.15)
    public static final String CHANNEL_ECOMMERCE = "ECOMMERCE";
    public static final String CHANNEL_ATM = "ATM";
    public static final String CHANNEL_POS = "POS";
    public static final String CHANNEL_INTERNATIONAL = "INTERNATIONAL";

    // Error codes
    public static final String CARD_NOT_FOUND = "CARD_NOT_FOUND";
    public static final String CARD_NOT_ACTIVE = "CARD_NOT_ACTIVE";
    public static final String CARD_BLOCKED = "CARD_BLOCKED";
    public static final String LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
    public static final String SERVICE_LIMIT_EXCEEDED = "SERVICE_LIMIT_EXCEEDED";
    public static final String INTERNATIONAL_DISABLED = "INTERNATIONAL_DISABLED";
    public static final String TRANSACTION_ON_HOLD = "TRANSACTION_ON_HOLD";
    public static final String TRANSACTION_NOT_FOUND = "TRANSACTION_NOT_FOUND";
    public static final String INVALID_CARD_TYPE = "INVALID_CARD_TYPE";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";

    private CreditCardConstants() {
    }
}
