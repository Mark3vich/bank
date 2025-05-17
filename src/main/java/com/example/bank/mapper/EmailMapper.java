package com.example.bank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.bank.dto.response.EmailInfoResponse;
import com.example.bank.model.EmailData;

@Mapper(componentModel = "spring")
public interface EmailMapper extends Mappable<EmailData, EmailInfoResponse> {
    @Override
    @Mapping(target = "user", ignore = true)
    EmailData toEntity(EmailInfoResponse dto);
}
