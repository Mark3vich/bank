package com.example.bank.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.bank.event.UserCreatedEvent;
import com.example.bank.event.UserUpdatedEvent;
import com.example.bank.model.User;
import com.example.bank.service.impl.UserNameCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserChangeListener {

    private final UserNameCacheService userNameCacheService;
    
    /**
     * Обработка события создания пользователя
     */
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreated(UserCreatedEvent event) {
        User user = event.getUser();
        log.info("User created, updating Redis cache: {}", user.getName());
        userNameCacheService.addUserToCache(user);
    }
    
    /**
     * Обработка события обновления пользователя
     */
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserUpdated(UserUpdatedEvent event) {
        User user = event.getUser();
        log.info("User updated, updating Redis cache: {}", user.getName());
        userNameCacheService.addUserToCache(user);
    }
} 