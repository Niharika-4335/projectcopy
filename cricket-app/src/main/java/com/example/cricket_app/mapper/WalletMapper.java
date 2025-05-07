package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.WalletResponse;
import com.example.cricket_app.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(target = "userId", source = "user.id")
    WalletResponse toResponseDto(Wallet wallet);
}
