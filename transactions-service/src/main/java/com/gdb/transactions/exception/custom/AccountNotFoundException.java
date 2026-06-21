package com.gdb.transactions.exception.custom;

public class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(String message) {
        super(message, "ACCOUNT_NOT_FOUND");
    }
}
