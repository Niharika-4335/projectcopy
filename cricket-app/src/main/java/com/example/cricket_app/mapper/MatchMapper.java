package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.request.CreateMatchRequest;
import com.example.cricket_app.dto.response.MatchResponse;
import com.example.cricket_app.entity.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchMapper {
    Match toEntity(CreateMatchRequest createMatchRequest);
    //dto to entity conversion to save into database.
    @Mapping(source = "id", target = "matchId")
    MatchResponse toResponseDto(Match match);
    //entity to dto to response..
}
