package com.gdb.account.constants;

/**
 * Global constants for the Account Service.
 */
public class AccountConstants {
    public static final String API_V1 = "/api/v1/accounts";
    public static final String INTERNAL_API_V1 = "/api/v1/internal/accounts";
    
    public static final String CURRENCY_INR = "INR";
    public static final String BANK_NAME_DEFAULT = "Global Digital Bank";
    public static final String BANK_BRANCH_DEFAULT = "Main Branch";
    public static final String IFSC_CODE_DEFAULT = "GDB0000001";
    
    public static final int BCRYPT_SALT_ROUNDS = 12;
    public static final long ACCOUNT_NUMBER_START = 1000L;
    
    // Error Codes
    public static final String ACCOUNT_NOT_FOUND = "ACCOUNT_NOT_FOUND";
    public static final String ACCOUNT_ALREADY_EXISTS = "ACCOUNT_ALREADY_EXISTS";
    public static final String ACCOUNT_INACTIVE = "ACCOUNT_INACTIVE";
    public static final String ACCOUNT_NOT_ACTIVE = "ACCOUNT_NOT_ACTIVE";
    public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String INSUFFICIENT_BALANCE = "INSUFFICIENT_BALANCE";
    public static final String INVALID_PIN = "INVALID_PIN";
    public static final String INVALID_AGE = "INVALID_AGE";
    public static final String DATABASE_ERROR = "DATABASE_ERROR";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    private AccountConstants() {}
}
