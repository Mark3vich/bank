package com.example.bank.mapper;

import org.mapstruct.Mapper;

import com.example.bank.dto.response.EmailInfoResponse;
import com.example.bank.model.EmailData;

@Mapper(componentModel = "spring", implementationName = "MapStructEmailMapperImpl")
public interface EmailMapper {
    EmailInfoResponse fromEmailData(EmailData emailData);
}
