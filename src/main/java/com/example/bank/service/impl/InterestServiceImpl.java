package com.example.bank.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bank.model.Account;
import com.example.bank.repository.AccountRepository;
import com.example.bank.service.InterestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {
    private static final Logger logger = LoggerFactory.getLogger(InterestServiceImpl.class);
    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.10"); // 10%
    private static final BigDecimal MAX_BALANCE_MULTIPLIER = new BigDecimal("2.07"); // 207%
    
    private final AccountRepository accountRepository;
    
    @Override
    @Transactional
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void applyInterestToAllAccounts() {
        logger.info("Starting to apply interest to all accounts");
        
        accountRepository.findAllWithUsers().forEach(this::applyInterest);
        
        logger.info("Finished applying interest to all accounts");
    }
    
    @Override
    @Transactional
    public void applyInterest(Account account) {
        // Get initial deposit or set it if not already set
        if (account.getInitialDeposit() == null) {
            recordInitialDeposit(account);
        }
        
        BigDecimal initialDeposit = account.getInitialDeposit();
        BigDecimal maxAllowedBalance = getMaximumAllowedBalance(initialDeposit);
        
        // Calculate interest
        BigDecimal currentBalance = account.getBalance();
        BigDecimal interest = currentBalance.multiply(INTEREST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = currentBalance.add(interest);
        
        // Check if new balance exceeds maximum allowed balance
        if (newBalance.compareTo(maxAllowedBalance) > 0) {
            logger.info("Account ID {} balance would exceed maximum allowed. Capping at {}% of initial deposit.",
                    account.getId(), MAX_BALANCE_MULTIPLIER.multiply(new BigDecimal(100)));
            account.setBalance(maxAllowedBalance);
        } else {
            account.setBalance(newBalance);
            logger.info("Applied {}% interest to Account ID {}. Balance: {} -> {}", 
                    INTEREST_RATE.multiply(new BigDecimal(100)),
                    account.getId(), currentBalance, newBalance);
        }
        
        accountRepository.save(account);
    }
    
    @Override
    @Transactional
    public void recordInitialDeposit(Account account) {
        // Check if initial deposit is already recorded
        if (account.getInitialDeposit() != null) {
            logger.info("Initial deposit already recorded for Account ID {}", account.getId());
            return;
        }
        
        // Record initial deposit
        account.setInitialDeposit(account.getBalance());
        accountRepository.save(account);
        
        logger.info("Recorded initial deposit of {} for Account ID {}", 
                account.getBalance(), account.getId());
    }
    
    @Override
    public BigDecimal getMaximumAllowedBalance(BigDecimal initialDeposit) {
        return initialDeposit.multiply(MAX_BALANCE_MULTIPLIER).setScale(2, RoundingMode.HALF_UP);
    }
} 