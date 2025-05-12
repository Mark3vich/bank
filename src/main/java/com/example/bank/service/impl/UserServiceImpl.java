package com.example.bank.service.impl;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bank.dto.request.EmailRequest;
import com.example.bank.dto.request.PhoneRequest;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.User;
import com.example.bank.repository.EmailRepository;
import com.example.bank.repository.PhoneRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.JwtService;
import com.example.bank.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final PhoneRepository phoneRepository;
    
    @Autowired
    private JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, 
                         EmailRepository emailRepository,
                         PhoneRepository phoneRepository) {
        this.userRepository = userRepository;
        this.emailRepository = emailRepository;
        this.phoneRepository = phoneRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> user;

        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier);
        } else {
            user = userRepository.findByPhone(identifier);
        }

        return user.orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }
    
    @Override
    @Transactional
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }
        
        String userIdentifier = authentication.getName();
        
        try {
            // Сначала пробуем найти пользователя с предзагрузкой токенов
            if (userIdentifier.contains("@")) {
                return userRepository.findByEmailWithTokens(userIdentifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by email with tokens"));
            } else {
                return userRepository.findByPhoneWithTokens(userIdentifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by phone with tokens"));
            }
        } catch (Exception e) {
            // Если не удалось, пробуем обычный метод
            if (userIdentifier.contains("@")) {
                return userRepository.findByEmail(userIdentifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by email"));
            } else {
                return userRepository.findByPhone(userIdentifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by phone"));
            }
        }
    }
    
    @Override
    @Transactional
    public User getUserFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("No valid Authorization header found");
        }
        
        String token = authHeader.substring(7);
        String identifier = jwtService.extractIdentifier(token);
        
        if (identifier == null || identifier.isEmpty()) {
            throw new IllegalArgumentException("Invalid token - no identifier found");
        }
        
        try {
            if (identifier.contains("@")) {
                return userRepository.findByEmailWithTokens(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by email from token"));
            } else {
                return userRepository.findByPhoneWithTokens(identifier)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by phone from token"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user from token: " + e.getMessage(), e);
        }
    }
    
    // Email operations
    @Override
    @Transactional
    public EmailData addEmail(User user, EmailRequest emailRequest) {
        if (existsByEmail(emailRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        EmailData emailData = new EmailData();
        emailData.setEmail(emailRequest.getEmail());
        emailData.setUser(user);
        
        user.addEmail(emailData);
        userRepository.save(user);
        
        return emailData;
    }
    
    @Override
    @Transactional
    public void updateEmail(User user, Long emailId, EmailRequest emailRequest) {
        // Проверка, что email принадлежит пользователю
        EmailData emailData = user.getEmails().stream()
                .filter(e -> e.getId().equals(emailId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Email not found or does not belong to user"));
        
        // Проверка, что новый email не занят
        if (!emailData.getEmail().equals(emailRequest.getEmail()) && 
                existsByEmail(emailRequest.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        
        emailData.setEmail(emailRequest.getEmail());
        emailRepository.save(emailData);
    }
    
    @Override
    @Transactional
    public void deleteEmail(User user, Long emailId) {
        // Проверка, что email принадлежит пользователю
        EmailData emailData = user.getEmails().stream()
                .filter(e -> e.getId().equals(emailId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Email not found or does not belong to user"));
        
        // Проверка, что у пользователя есть хотя бы один другой email
        if (user.getEmails().size() <= 1) {
            throw new IllegalStateException("Cannot delete the only email. User must have at least one email.");
        }
        
        user.removeEmail(emailData);
        userRepository.save(user);
    }
    
    // Phone operations
    @Override
    @Transactional
    public PhoneData addPhone(User user, PhoneRequest phoneRequest) {
        if (existsByPhone(phoneRequest.getPhone())) {
            throw new IllegalArgumentException("Phone is already in use");
        }
        
        PhoneData phoneData = new PhoneData();
        phoneData.setPhone(phoneRequest.getPhone());
        phoneData.setUser(user);
        
        user.addPhone(phoneData);
        userRepository.save(user);
        
        return phoneData;
    }
    
    @Override
    @Transactional
    public void updatePhone(User user, Long phoneId, PhoneRequest phoneRequest) {
        // Проверка, что телефон принадлежит пользователю
        PhoneData phoneData = user.getPhones().stream()
                .filter(p -> p.getId().equals(phoneId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Phone not found or does not belong to user"));
        
        // Проверка, что новый телефон не занят
        if (!phoneData.getPhone().equals(phoneRequest.getPhone()) && 
                existsByPhone(phoneRequest.getPhone())) {
            throw new IllegalArgumentException("Phone is already in use");
        }
        
        phoneData.setPhone(phoneRequest.getPhone());
        phoneRepository.save(phoneData);
    }
    
    @Override
    @Transactional
    public void deletePhone(User user, Long phoneId) {
        // Проверка, что телефон принадлежит пользователю
        PhoneData phoneData = user.getPhones().stream()
                .filter(p -> p.getId().equals(phoneId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Phone not found or does not belong to user"));
        
        // Проверка, что у пользователя есть хотя бы один другой телефон
        if (user.getPhones().size() <= 1) {
            throw new IllegalStateException("Cannot delete the only phone. User must have at least one phone.");
        }
        
        user.removePhone(phoneData);
        userRepository.save(user);
    }
}
