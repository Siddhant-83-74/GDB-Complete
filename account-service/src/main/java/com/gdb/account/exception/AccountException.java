package com.gdb.account.exception;

import lombok.Getter;

/**
 * Base exception for Account Service.
 */
@Getter
public class AccountException extends RuntimeException {
    private final String errorCode;

    public AccountException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
