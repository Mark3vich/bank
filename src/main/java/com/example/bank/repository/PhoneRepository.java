package com.example.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bank.model.PhoneData;

public interface PhoneRepository extends JpaRepository<PhoneData, Long> {
    boolean existsByPhone(String phone);
} 