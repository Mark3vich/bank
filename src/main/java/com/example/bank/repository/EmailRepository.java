package com.example.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bank.model.EmailData;

public interface EmailRepository extends JpaRepository<EmailData, Long> {
    boolean existsByEmail(String email);
} 