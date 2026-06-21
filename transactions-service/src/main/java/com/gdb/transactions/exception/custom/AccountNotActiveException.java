package com.gdb.transactions.exception.custom;

public class AccountNotActiveException extends BusinessException {
    public AccountNotActiveException(String message) {
        super(message, "ACCOUNT_NOT_ACTIVE");
    }
}
