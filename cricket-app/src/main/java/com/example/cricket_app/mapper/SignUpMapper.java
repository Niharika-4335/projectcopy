package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.SignUpResponse;
import com.example.cricket_app.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SignUpMapper {
    @Mapping(source = "id", target = "id")
    SignUpResponse toResponseDto(Users user);

}
