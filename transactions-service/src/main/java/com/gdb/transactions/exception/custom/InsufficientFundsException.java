package com.gdb.transactions.exception.custom;

public class InsufficientFundsException extends BusinessException {
    public InsufficientFundsException(String message) {
        super(message, "INSUFFICIENT_FUNDS");
    }
}
