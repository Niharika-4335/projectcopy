package com.example.cricket_app.service;

import com.example.cricket_app.dto.response.PayOutSummaryResponse;

public interface PayOutService {
    PayOutSummaryResponse processPayout(Long matchId);
}
