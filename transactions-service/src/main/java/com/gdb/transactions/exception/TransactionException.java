package com.gdb.transactions.exception;

/**
 * Base exception class for all transaction-related exceptions.
 */
public class TransactionException extends RuntimeException {
    private final String errorCode;

    public TransactionException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public TransactionException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}