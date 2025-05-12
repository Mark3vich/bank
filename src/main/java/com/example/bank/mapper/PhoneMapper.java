package com.example.bank.mapper;

import org.mapstruct.Mapper;

import com.example.bank.dto.response.PhoneInfoResponse;
import com.example.bank.model.PhoneData;

@Mapper(componentModel = "spring", implementationName = "MapStructPhoneMapperImpl")
public interface PhoneMapper {
    PhoneInfoResponse fromPhoneData(PhoneData phoneData);
}
