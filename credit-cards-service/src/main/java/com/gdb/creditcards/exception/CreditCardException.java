package com.gdb.creditcards.exception;

import lombok.Getter;

/**
 * Base business exception for the Credit Cards Service.
 */
@Getter
public class CreditCardException extends RuntimeException {
    private final String errorCode;

    public CreditCardException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
