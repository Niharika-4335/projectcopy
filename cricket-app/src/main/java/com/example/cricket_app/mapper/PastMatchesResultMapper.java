package com.example.cricket_app.mapper;

import com.example.cricket_app.dto.response.PastMatchesResultResponse;
import com.example.cricket_app.entity.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PastMatchesResultMapper {
    @Mapping(target = "matchId", source = "id")
    @Mapping(target = "matchStatus", source = "status")
    PastMatchesResultResponse toResponseDto(Match match);//mapstruct only works on single objects not lists

    List<PastMatchesResultResponse> toResponseDtoList(List<Match> matchs);
}
