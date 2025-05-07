package com.example.cricket_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedBetResponse {
    private List<BetResponse> bets;
    private int currentPage;
    private int totalPages;
    private long totalUsers;


}
