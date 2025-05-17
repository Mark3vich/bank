package com.example.bank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.bank.dto.response.PhoneInfoResponse;
import com.example.bank.model.PhoneData;

@Mapper(componentModel = "spring")
public interface PhoneMapper extends Mappable<PhoneData, PhoneInfoResponse> {
    @Override
    @Mapping(target = "user", ignore = true)
    PhoneData toEntity(PhoneInfoResponse dto);
}
