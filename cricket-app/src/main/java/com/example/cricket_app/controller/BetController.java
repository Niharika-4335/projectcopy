package com.example.cricket_app.controller;

import com.example.cricket_app.dto.request.BetRequest;
import com.example.cricket_app.dto.response.BetResponse;
import com.example.cricket_app.dto.response.PagedBetResponse;
import com.example.cricket_app.service.BetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bet")
public class BetController {
    private final BetService betService;

    @Autowired
    public BetController(BetService betService) {
        this.betService = betService;
    }


    @PreAuthorize("hasRole('PLAYER')")
    @PostMapping("/create")
    public ResponseEntity<BetResponse> placeBet(@RequestBody BetRequest request) {
        BetResponse response = betService.placeBet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('PLAYER')")
    @GetMapping("/history")
    public PagedBetResponse getBetHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        return betService.getUserBetHistory(page, size, sortBy, direction);
    }
}
