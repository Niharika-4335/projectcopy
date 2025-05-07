package com.example.cricket_app.service;

import com.example.cricket_app.dto.request.BetRequest;
import com.example.cricket_app.dto.response.BetResponse;
import com.example.cricket_app.dto.response.PagedBetResponse;

public interface BetService {

    BetResponse placeBet(BetRequest request);

    PagedBetResponse getUserBetHistory(int page, int size, String sortBy, String direction);

    void updateBetStatusesForMatchWinner(Long matchId);
}
