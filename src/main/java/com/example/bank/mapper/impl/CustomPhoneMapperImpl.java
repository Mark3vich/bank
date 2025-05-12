package com.example.bank.mapper.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.example.bank.dto.response.PhoneInfoResponse;
import com.example.bank.mapper.PhoneMapper;
import com.example.bank.model.PhoneData;

@Primary
@Component("customPhoneMapper")
public class CustomPhoneMapperImpl implements PhoneMapper {

    @Override
    public PhoneInfoResponse fromPhoneData(PhoneData phoneData) {
        if (phoneData == null) {
            return null;
        }
        
        return PhoneInfoResponse.builder()
                .id(phoneData.getId())
                .phone(phoneData.getPhone())
                .formattedPhone(phoneData.getFormattedPhone())
                .build();
    }
} 