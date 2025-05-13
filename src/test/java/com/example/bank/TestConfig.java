package com.example.bank;

import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Set;

@Configuration
@TestConfiguration
@ComponentScan("com.example.bank")
@EntityScan("com.example.bank.model")
@EnableJpaRepositories("com.example.bank.repository")
@EnableTransactionManagement
@EnableAutoConfiguration
public class TestConfig {
    
    // Mock the Redis connection factory to avoid actual Redis connection
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;
    
    // Provide a no-operation cache manager for tests
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }
    
    // Password encoder for user creation in tests
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Мок RedisTemplate для тестов - главный бин для строковых значений
    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> mockRedisTemplate = Mockito.mock(RedisTemplate.class);
        
        // Настраиваем поведение для метода keys (возвращаем пустой Set)
        Mockito.when(mockRedisTemplate.keys(Mockito.anyString())).thenReturn(Collections.emptySet());
        
        // Мокаем ZSetOperations
        ZSetOperations<String, String> mockZSetOps = Mockito.mock(ZSetOperations.class);
        Mockito.when(mockRedisTemplate.opsForZSet()).thenReturn(mockZSetOps);
        Mockito.when(mockZSetOps.add(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble()))
               .thenReturn(true);
        
        // Мокаем ValueOperations для методов opsForValue
        var mockValueOps = Mockito.mock(org.springframework.data.redis.core.ValueOperations.class);
        Mockito.when(mockRedisTemplate.opsForValue()).thenReturn(mockValueOps);
        
        // Мокаем RedisConnectionFactory
        var mockConnFactory = Mockito.mock(RedisConnectionFactory.class);
        Mockito.when(mockRedisTemplate.getConnectionFactory()).thenReturn(mockConnFactory);
        
        // Мокаем RedisConnection
        var mockConn = Mockito.mock(org.springframework.data.redis.connection.RedisConnection.class);
        Mockito.when(mockConnFactory.getConnection()).thenReturn(mockConn);
        Mockito.when(mockConn.ping()).thenThrow(new RuntimeException("Redis not available in test mode"));
        
        return mockRedisTemplate;
    }
    
    // StringRedisTemplate - без @Primary
    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        return Mockito.mock(StringRedisTemplate.class);
    }
    
    // Мок ApplicationEventPublisher для публикации событий
    @Bean
    @Primary
    public ApplicationEventPublisher applicationEventPublisher() {
        return Mockito.mock(ApplicationEventPublisher.class);
    }
} 