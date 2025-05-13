package com.example.bank.exception;

public class SelfTransferException extends BusinessException {
    public SelfTransferException(String message) {
        super(message);
    }
}
