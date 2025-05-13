package com.example.bank.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.example.bank.event.UserCreatedEvent;
import com.example.bank.event.UserUpdatedEvent;
import com.example.bank.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Сервис для публикации событий пользователя
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    
    /**
     * Публикует событие создания пользователя
     */
    public void publishUserCreated(User user) {
        log.debug("Publishing user created event for user: {}", user.getId());
        eventPublisher.publishEvent(new UserCreatedEvent(user));
    }
    
    /**
     * Публикует событие обновления пользователя
     */
    public void publishUserUpdated(User user) {
        log.debug("Publishing user updated event for user: {}", user.getId());
        eventPublisher.publishEvent(new UserUpdatedEvent(user));
    }
} 