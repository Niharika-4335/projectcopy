package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.WalletTransactionResponse;
import com.example.cricket_app.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(source = "wallet.user.id", target = "userId")
    @Mapping(source = "match.id", target = "matchId")
    WalletTransactionResponse toResponseDto(WalletTransaction transaction);

    List<WalletTransactionResponse> toResponseDtoList(List<WalletTransaction> transactions);
    //source->entity side target->dto side.

}
