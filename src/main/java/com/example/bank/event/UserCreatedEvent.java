package com.example.bank.event;

import com.example.bank.model.User;

import lombok.Getter;

/**
 * Событие создания пользователя
 */
public class UserCreatedEvent {

    @Getter
    private final User user;

    public UserCreatedEvent(User user) {
        this.user = user;
    }
}