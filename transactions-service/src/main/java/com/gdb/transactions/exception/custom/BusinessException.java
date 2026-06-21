package com.gdb.transactions.exception.custom;

import com.gdb.transactions.exception.TransactionException;
import lombok.Getter;

@Getter
public class BusinessException extends TransactionException {

    public BusinessException(String message, String errorCode) {
        super(errorCode, message);
    }
}
