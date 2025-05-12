package com.example.bank.service;

import java.math.BigDecimal;

import com.example.bank.model.Account;

public interface InterestService {
    /**
     * Applies interest to all accounts
     */
    void applyInterestToAllAccounts();
    
    /**
     * Calculates and applies interest to a specific account
     * @param account The account to apply interest to
     */
    void applyInterest(Account account);
    
    /**
     * Records initial deposit for a new account
     * @param account The account to record initial deposit for
     */
    void recordInitialDeposit(Account account);
    
    /**
     * Gets the maximum balance allowed for an account based on initial deposit
     * @param initialDeposit The initial deposit amount
     * @return The maximum balance allowed (207% of initial deposit)
     */
    BigDecimal getMaximumAllowedBalance(BigDecimal initialDeposit);
} 