package com.example.bank.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.bank.dto.request.EmailRequest;
import com.example.bank.dto.request.PhoneRequest;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.User;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService extends UserDetailsService {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    
    // Email operations
    EmailData addEmail(User user, EmailRequest emailRequest);
    void updateEmail(User user, Long emailId, EmailRequest emailRequest);
    void deleteEmail(User user, Long emailId);
    
    // Phone operations
    PhoneData addPhone(User user, PhoneRequest phoneRequest);
    void updatePhone(User user, Long phoneId, PhoneRequest phoneRequest);
    void deletePhone(User user, Long phoneId);
    
    // Get current user
    User getCurrentUser();
    
    // Get user from token in the request
    User getUserFromRequest(HttpServletRequest request);
}
