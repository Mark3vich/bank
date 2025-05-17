package com.example.bank.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.bank.model.User;
import com.example.bank.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserNameCacheService {
    private static final String USER_NAME_PREFIX = "user:name:";
    private static final String USER_ID_PREFIX = "user:id:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    
    /**
     * Инициализирует кэш имен пользователей при старте приложения
     */
    @PostConstruct
    public void initializeCache() {
        try {
            log.info("Initializing user names cache in Redis");
            
            // Проверяем доступность Redis
            try {
                var connectionFactory = redisTemplate.getConnectionFactory();
                if (connectionFactory != null) {
                    var connection = connectionFactory.getConnection();
                    if (connection != null) {
                        connection.ping();
                    } else {
                        log.warn("Redis connection is null. Cache initialization skipped.");
                        return;
                    }
                } else {
                    log.warn("Redis connection factory is null. Cache initialization skipped.");
                    return;
                }
            } catch (Exception e) {
                log.warn("Redis is not available. Cache initialization skipped. Error: {}", e.getMessage());
                return;
            }
            
            List<User> allUsers = userRepository.findAll();
            
            // Сохраняем имена всех пользователей
            for (User user : allUsers) {
                addUserToCache(user);
            }
            
            log.info("Cached {} user names in Redis", allUsers.size());
        } catch (Exception e) {
            log.error("Failed to initialize Redis cache for user names", e);
        }
    }
    
    /**
     * Добавляет пользователя в кэш
     */
    public void addUserToCache(User user) {
        if (user == null || user.getName() == null || user.getId() == null) {
            log.warn("Cannot add invalid user to cache");
            return;
        }
        
        try {
            // Проверяем доступность Redis
            try {
                var connectionFactory = redisTemplate.getConnectionFactory();
                 if (connectionFactory != null) {
                    var connection = connectionFactory.getConnection();
                    if (connection != null) {
                        connection.ping();
                    } else {
                        log.warn("Redis connection is null. User {} not cached. Error: {}", user.getName(), "Connection is null");
                        return;
                    }
                } else {
                    log.warn("Redis connection factory is null. User {} not cached. Error: {}", user.getName(), "Connection factory is null");
                    return;
                }
            } catch (Exception e) {
                log.warn("Redis is not available. User {} not cached. Error: {}", user.getName(), e.getMessage());
                return;
            }
            
            // Сохраняем имя по ID
            String userNameKey = USER_NAME_PREFIX + user.getId();
            redisTemplate.opsForValue().set(userNameKey, user.getName());
            
            // Сохраняем ID по имени
            String userIdKey = USER_ID_PREFIX + user.getName();
            redisTemplate.opsForValue().set(userIdKey, user.getId().toString());
            
            log.debug("Added user to Redis cache: id={}, name={}", user.getId(), user.getName());
        } catch (Exception e) {
            log.error("Failed to add user to Redis cache: {}", user.getName(), e);
        }
    }
    
    /**
     * Поиск пользователей по части имени
     */
    public Set<String> findUserNamesByPrefix(String prefix) {
        try {
            if (prefix == null || prefix.isEmpty()) {
                return Collections.emptySet();
            }
            
            // Проверяем доступность Redis
            try {
                var connectionFactory = redisTemplate.getConnectionFactory();
                 if (connectionFactory != null) {
                    var connection = connectionFactory.getConnection();
                    if (connection != null) {
                        connection.ping();
                    } else {
                         log.warn("Redis connection is null. Returning empty results for prefix: {}. Error: {}", prefix, "Connection is null");
                        return Collections.emptySet();
                    }
                } else {
                    log.warn("Redis connection factory is null. Returning empty results for prefix: {}. Error: {}", prefix, "Connection factory is null");
                    return Collections.emptySet();
                }
            } catch (Exception e) {
                log.warn("Redis is not available. Returning empty results for prefix: {}. Error: {}", prefix, e.getMessage());
                return Collections.emptySet();
            }
            
            // Формируем регистронезависимый поиск (приводим к нижнему регистру)
            String prefixLower = prefix.toLowerCase();
            
            // Ищем все ключи с именами пользователей
            Set<String> allUserNameKeys = redisTemplate.keys(USER_ID_PREFIX + "*");
            
            // Вытаскиваем только имена из ключей и фильтруем по префиксу
            Set<String> matchingNames = allUserNameKeys.stream()
                    .map(key -> key.substring(USER_ID_PREFIX.length()))
                    .filter(name -> name.toLowerCase().startsWith(prefixLower))
                    .collect(Collectors.toSet());
            
            log.info("Found {} user names with prefix: '{}'", matchingNames.size(), prefix);
            return matchingNames;
        } catch (Exception e) {
            log.error("Failed to search users by prefix in Redis: {}", prefix, e);
            return Collections.emptySet();
        }
    }
    
    /**
     * Получить ID пользователей по именам
     */
    public List<Long> getUserIdsByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // Проверяем доступность Redis
            try {
                var connectionFactory = redisTemplate.getConnectionFactory();
                 if (connectionFactory != null) {
                    var connection = connectionFactory.getConnection();
                    if (connection != null) {
                        connection.ping();
                    } else {
                        log.warn("Redis connection is null. Returning empty results for user IDs. Error: {}", "Connection is null");
                        return Collections.emptyList();
                    }
                } else {
                    log.warn("Redis connection factory is null. Returning empty results for user IDs. Error: {}", "Connection factory is null");
                    return Collections.emptyList();
                }
            } catch (Exception e) {
                log.warn("Redis is not available. Returning empty results for user IDs. Error: {}", e.getMessage());
                return Collections.emptyList();
            }
            
            return names.stream()
                .map(name -> USER_ID_PREFIX + name)
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(idStr -> idStr != null)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Failed to get user IDs by names from Redis", e);
            return Collections.emptyList();
        }
    }
} 