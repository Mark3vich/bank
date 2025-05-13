package com.example.bank.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import com.example.bank.model.TransactionLog;
import com.example.bank.repository.TransactionLogRepository;
import com.example.bank.service.TransactionLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionLogServiceImpl implements TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransfer(Long senderId, Long recipientId, BigDecimal amount, String description) {
        TransactionLog log = new TransactionLog();
        log.setSenderId(senderId);
        log.setRecipientId(recipientId);
        log.setAmount(amount);
        log.setDescription(description);
        log.setTimestamp(LocalDateTime.now());
        
        transactionLogRepository.save(log);
    }
}
