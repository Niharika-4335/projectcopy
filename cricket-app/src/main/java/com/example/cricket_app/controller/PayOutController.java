package com.example.cricket_app.controller;

import com.example.cricket_app.dto.request.DeclareWinnerRequest;
import com.example.cricket_app.dto.response.PayOutSummaryResponse;
import com.example.cricket_app.dto.response.PayoutResponse;
import com.example.cricket_app.service.MatchService;
import com.example.cricket_app.service.PayOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payout")
public class PayOutController {
    private final MatchService matchService;
    private final PayOutService payOutService;

    @Autowired
    public PayOutController(MatchService matchService, PayOutService payOutService) {
        this.matchService = matchService;
        this.payOutService = payOutService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/declare-winner")
    public List<PayoutResponse> declareMatchWinner(@RequestBody DeclareWinnerRequest request) {
        return matchService.declareMatchWinner(request.getMatchId(), String.valueOf(request.getWinningTeam()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payoutSummary/{matchId}")
    public PayOutSummaryResponse getPayoutSummary(@PathVariable Long matchId) {
        return payOutService.processPayout(matchId);
    }


}
