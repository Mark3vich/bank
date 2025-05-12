package com.example.bank.mapper;

import org.mapstruct.Mapper;

import com.example.bank.dto.response.UserInfoResponse;
import com.example.bank.model.User;

@Mapper(componentModel = "spring", implementationName = "MapStructUserMapperImpl", uses = {EmailMapper.class, PhoneMapper.class})
public interface UserMapper {
    UserInfoResponse fromUser(User user);
}
