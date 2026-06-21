package com.gdb.transactions.exception.custom;

public class ServiceUnavailableException extends BusinessException {
    public ServiceUnavailableException(String message) {
        super(message, "SERVICE_UNAVAILABLE");
    }
}
