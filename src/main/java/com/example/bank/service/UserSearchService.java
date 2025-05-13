package com.example.bank.service;

import com.example.bank.dto.request.UserSearchRequest;
import com.example.bank.dto.response.UserSearchResponse;

public interface UserSearchService {
    /**
     * Поиск пользователей с фильтрацией и пагинацией
     * 
     * @param request параметры поиска и пагинации
     * @return результат поиска с пагинацией
     */
    UserSearchResponse searchUsers(UserSearchRequest request);
} 