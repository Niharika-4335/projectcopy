package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.BetResponse;
import com.example.cricket_app.entity.Bet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BetMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "match.id", target = "matchId")
    @Mapping(source = "status", target = "status")
    BetResponse toResponse(Bet bet);//we are showing match id from match.

}
