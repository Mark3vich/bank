package com.example.bank.event;

import com.example.bank.model.User;

import lombok.Getter;

/**
 * Событие обновления пользователя
 */
public class UserUpdatedEvent {

    @Getter
    private final User user;

    public UserUpdatedEvent(User user) {
        this.user = user;
    }
}