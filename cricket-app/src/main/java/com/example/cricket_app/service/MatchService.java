package com.example.cricket_app.service;

import com.example.cricket_app.dto.request.CreateMatchRequest;
import com.example.cricket_app.dto.response.MatchResponse;
import com.example.cricket_app.dto.response.PagedUpcomingMatchResponse;
import com.example.cricket_app.dto.response.PastMatchesResultResponse;
import com.example.cricket_app.dto.response.PayoutResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MatchService {
    MatchResponse createMatch(CreateMatchRequest createMatchRequest) throws BadRequestException;

    PagedUpcomingMatchResponse getUpcomingMatches(Pageable pageable);

    List<PayoutResponse> declareMatchWinner(Long matchId, String winningTeam);

    List<PastMatchesResultResponse> viewPastMatchesResults();

}
