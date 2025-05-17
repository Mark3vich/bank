package com.example.bank.mapper;

import org.mapstruct.Mapper;

import com.example.bank.dto.response.UserInfoResponse;
import com.example.bank.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper extends Mappable<User, UserInfoResponse> {

}
