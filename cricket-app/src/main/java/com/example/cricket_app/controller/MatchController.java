package com.example.cricket_app.controller;

import com.example.cricket_app.dto.request.CreateMatchRequest;
import com.example.cricket_app.dto.response.MatchResponse;
import com.example.cricket_app.dto.response.PagedUpcomingMatchResponse;
import com.example.cricket_app.dto.response.PastMatchesResultResponse;
import com.example.cricket_app.service.MatchService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    @Autowired
    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-match")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody CreateMatchRequest request) throws BadRequestException {
        MatchResponse createdMatch = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }

    @PreAuthorize("hasRole('PLAYER')")
    @GetMapping("/upcoming")
    public PagedUpcomingMatchResponse getUpcomingMatches(Pageable pageable) {
        return matchService.getUpcomingMatches(pageable);

    }

    @PreAuthorize("hasRole('PLAYER')")
    @GetMapping("/completed")
    public ResponseEntity<List<PastMatchesResultResponse>> getCompletedMatches() {
        List<PastMatchesResultResponse> pastMatchesResultResponses = matchService.viewPastMatchesResults();
        return ResponseEntity.ok(pastMatchesResultResponses);

    }




}
