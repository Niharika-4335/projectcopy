package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.PayoutResponse;
import com.example.cricket_app.entity.Payout;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayOutMapper {
    @Mapping(source = "amount", target = "payoutPerWinner")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "match.id", target = "matchId")
    @Mapping(source = "processedAt", target = "processedAt")
    PayoutResponse toResponse(Payout payout);
}
