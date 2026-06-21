package com.gdb.transactions.exception.custom;

public class InvalidPinException extends BusinessException {
    public InvalidPinException(String message) {
        super(message, "INVALID_PIN");
    }
}
