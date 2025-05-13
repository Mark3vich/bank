package com.example.bank.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.bank.dto.request.UserSearchRequest;
import com.example.bank.dto.response.UserDto;
import com.example.bank.dto.response.UserSearchResponse;
import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;
import com.example.bank.repository.UserSearchRepository;
import com.example.bank.service.UserSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSearchServiceImpl implements UserSearchService {

    private final UserSearchRepository userSearchRepository;
    private final UserRepository userRepository;
    private final UserNameCacheService userNameCacheService;

    @Override
    public UserSearchResponse searchUsers(UserSearchRequest request) {
        log.info("Searching users with filters: {}", request);
        
        // Пагинация
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0, 
                request.getSize() != null ? request.getSize() : 10);
        
        Page<User> resultPage;
        
        // Если есть поиск по имени, используем Redis
        if (request.getName() != null && !request.getName().isEmpty()) {
            resultPage = searchByNameUsingRedis(request, pageable);
        } else {
            // Иначе используем обычный поиск с фильтрами
            resultPage = userSearchRepository.findUsersByFilters(
                    null, // name не используем напрямую
                    request.getName() != null ? request.getName() + "%" : null, // namePattern для LIKE
                    request.getEmail(),
                    request.getPhone(),
                    request.getDateOfBirth(),
                    pageable);
        }
        
        // Преобразуем результаты в DTO
        List<UserDto> userDtos = resultPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        // Формируем ответ с пагинацией
        return UserSearchResponse.builder()
                .users(userDtos)
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .currentPage(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .build();
    }
    
    /**
     * Поиск пользователей по имени с использованием Redis
     */
    private Page<User> searchByNameUsingRedis(UserSearchRequest request, Pageable pageable) {
        log.info("Searching users by name prefix in Redis: {}", request.getName());
        
        // Поиск имен в Redis по префиксу
        Set<String> matchingNames = userNameCacheService.findUserNamesByPrefix(request.getName());
        
        if (matchingNames.isEmpty()) {
            log.info("No matching names found in Redis for prefix: {}", request.getName());
            return Page.empty(pageable);
        }
        
        log.info("Found matching names in Redis: {}", matchingNames);
        
        // Получение ID пользователей по найденным именам
        List<Long> userIds = userNameCacheService.getUserIdsByNames(matchingNames);
        
        if (userIds.isEmpty()) {
            log.info("No user IDs found in Redis for the matched names");
            return Page.empty(pageable);
        }
        
        log.info("Found user IDs in Redis: {}", userIds);
        
        // Ищем пользователей только с точным соответствием имени по найденным именам
        // В SQL это будет условие WHERE u.name IN (имена из Redis)
        List<User> matchedUsers = userRepository.findAllById(userIds);
        
        // Дополнительно фильтруем по имени программно
        List<User> filteredUsers = matchedUsers.stream()
                .filter(user -> user.getName() != null && 
                        user.getName().toLowerCase().startsWith(request.getName().toLowerCase()))
                .collect(Collectors.toList());
        
        log.info("After name filtering: {} users matched", filteredUsers.size());
        
        // Фильтруем по другим критериям
        List<User> finalFilteredUsers = filteredUsers.stream()
                .filter(user -> filterByEmail(user, request.getEmail()))
                .filter(user -> filterByPhone(user, request.getPhone()))
                .filter(user -> filterByDateOfBirth(user, request.getDateOfBirth()))
                .collect(Collectors.toList());
        
        log.info("After all filters: {} users matched", finalFilteredUsers.size());
        
        // Создаем Page из результатов
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), finalFilteredUsers.size());
        
        if (start > finalFilteredUsers.size()) {
            return Page.empty(pageable);
        }
        
        return new org.springframework.data.domain.PageImpl<>(
                finalFilteredUsers.subList(start, end),
                pageable,
                finalFilteredUsers.size());
    }
    
    // Вспомогательные методы для фильтрации

    private boolean filterByEmail(User user, String email) {
        if (email == null || email.isEmpty()) {
            return true; // если email не указан, фильтр не применяем
        }
        return user.getEmails().stream()
                .anyMatch(e -> email.equals(e.getEmail()));
    }

    private boolean filterByPhone(User user, String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; // если телефон не указан, фильтр не применяем
        }
        return user.getPhones().stream()
                .anyMatch(p -> phone.equals(p.getPhone()));
    }

    private boolean filterByDateOfBirth(User user, String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.isEmpty()) {
            return true; // если дата не указана, фильтр не применяем
        }
        return user.getDateOfBirth() != null && user.getDateOfBirth().compareTo(dateOfBirth) > 0;
    }
    
    /**
     * Преобразование сущности User в DTO
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .dateOfBirth(user.getDateOfBirth())
                .emails(user.getEmails().stream()
                        .map(e -> e.getEmail())
                        .collect(Collectors.toList()))
                .phones(user.getPhones().stream()
                        .map(p -> p.getPhone())
                        .collect(Collectors.toList()))
                .balance(user.getAccount() != null ? user.getAccount().getBalance() : null)
                .build();
    }
} 