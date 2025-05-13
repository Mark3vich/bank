package com.example.bank.service;

import java.math.BigDecimal;

public interface TransactionLogService {
    void logTransfer(Long senderId, Long recipientId, BigDecimal amount, String description);
}
