package com.example.bank.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.bank.dto.response.UserInfoResponse;
import com.example.bank.model.User;

@Mapper(componentModel = "spring", uses = { PhoneMapper.class, EmailMapper.class })
public interface UserMapper extends Mappable<User, UserInfoResponse> {
    @Override
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    User toEntity(UserInfoResponse dto);
}
