package com.example.bank.mapper.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.bank.dto.response.EmailInfoResponse;
import com.example.bank.dto.response.PhoneInfoResponse;
import com.example.bank.dto.response.UserInfoResponse;
import com.example.bank.mapper.EmailMapper;
import com.example.bank.mapper.PhoneMapper;
import com.example.bank.mapper.UserMapper;
import com.example.bank.model.User;

@Primary
@Component("customUserMapper")
public class CustomUserMapperImpl implements UserMapper {
    
    private final EmailMapper emailMapper;
    private final PhoneMapper phoneMapper;
    
    @Autowired
    public CustomUserMapperImpl(EmailMapper emailMapper, PhoneMapper phoneMapper) {
        this.emailMapper = emailMapper;
        this.phoneMapper = phoneMapper;
    }

    @Override
    public UserInfoResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        
        List<EmailInfoResponse> emails = user.getEmails().stream()
                .map(emailMapper::fromEmailData)
                .collect(Collectors.toList());
                
        List<PhoneInfoResponse> phones = user.getPhones().stream()
                .map(phoneMapper::fromPhoneData)
                .collect(Collectors.toList());
                
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dateOfBirth(user.getDateOfBirth())
                .emails(emails)
                .phones(phones)
                .build();
    }
} 