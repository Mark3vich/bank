package com.example.bank.mapper.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.bank.dto.response.EmailInfoResponse;
import com.example.bank.mapper.EmailMapper;
import com.example.bank.model.EmailData;

@Primary
@Component("customEmailMapper")
public class CustomEmailMapperImpl implements EmailMapper {

    @Override
    public EmailInfoResponse fromEmailData(EmailData emailData) {
        if (emailData == null) {
            return null;
        }
        
        return EmailInfoResponse.builder()
                .id(emailData.getId())
                .email(emailData.getEmail())
                .build();
    }
} 