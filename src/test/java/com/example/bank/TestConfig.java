package com.example.bank;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("com.example.bank")
@EntityScan("com.example.bank.model")
@EnableJpaRepositories("com.example.bank.repository")
@EnableTransactionManagement
@EnableAutoConfiguration
public class TestConfig {
} 