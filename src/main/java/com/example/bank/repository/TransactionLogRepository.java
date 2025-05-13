package com.example.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bank.model.TransactionLog;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    
}
