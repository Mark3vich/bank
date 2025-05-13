package com.example.bank.service.impl;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.example.bank.dto.request.TransferRequest;
import com.example.bank.exception.AccountNotFoundException;
import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.InvalidAmountException;
import com.example.bank.exception.SelfTransferException;
import com.example.bank.model.Account;
import com.example.bank.model.User;
import com.example.bank.repository.AccountRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.JwtService;
import com.example.bank.service.TransactionLogService;
import com.example.bank.service.TransferService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionLogService transactionLogService;
    private final JwtService jwtService;

    @Retryable(retryFor = ObjectOptimisticLockingFailureException.class, maxAttempts = 3)
    @Transactional
    public void transferMoney(TransferRequest request, HttpServletRequest httpRequest) {
        // Извлекаем токен из заголовков запроса
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AccountNotFoundException("Authorization header missing or invalid");
        }
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Извлекаем идентификатор пользователя (email или телефон) из токена
        String identifier = jwtService.extractIdentifier(token);
        if (identifier == null) {
            throw new AccountNotFoundException("User identifier could not be extracted from token");
        }
        
        // Находим пользователя по идентификатору (email)
        User senderUser;
        if (identifier.contains("@")) {
            // Это email
            senderUser = userRepository.findByEmailWithTokens(identifier)
                    .orElseThrow(() -> new AccountNotFoundException("Sender not found with email: " + identifier));
        } else {
            // Это телефон
            senderUser = userRepository.findByPhoneWithTokens(identifier)
                    .orElseThrow(() -> new AccountNotFoundException("Sender not found with phone: " + identifier));
        }
        
        Long senderId = senderUser.getId();

        // Проверяем, что пользователь не переводит сам себе
        if (Objects.equals(senderId, request.getRecipientId())) {
            throw new SelfTransferException("Cannot transfer money to yourself");
        }

        // Получаем аккаунты с блокировками для избежания race condition
        Account senderAccount = accountRepository.findByUserIdWithLock(senderId)
                .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        Account recipientAccount = accountRepository.findByUserIdWithLock(request.getRecipientId())
                .orElseThrow(() -> new AccountNotFoundException("Recipient account not found"));

        // Проверяем достаточность средств
        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // Проверяем лимиты перевода
        validateTransferLimits(request.getAmount());

        // Выполняем перевод
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));
        recipientAccount.setBalance(recipientAccount.getBalance().add(request.getAmount()));

        accountRepository.save(senderAccount);
        accountRepository.save(recipientAccount);

        // Логируем транзакцию
        transactionLogService.logTransfer(
                senderId,
                request.getRecipientId(),
                request.getAmount(),
                "Transfer between accounts");
    }

    private void validateTransferLimits(BigDecimal amount) {
        BigDecimal maxTransferAmount = new BigDecimal("1000000");
        BigDecimal minTransferAmount = new BigDecimal("1");

        if (amount.compareTo(minTransferAmount) < 0) {
            throw new InvalidAmountException("Amount is too small");
        }

        if (amount.compareTo(maxTransferAmount) > 0) {
            throw new InvalidAmountException("Amount exceeds maximum transfer limit");
        }
    }
}
