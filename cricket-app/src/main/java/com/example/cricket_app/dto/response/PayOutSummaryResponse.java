package com.example.cricket_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class PayOutSummaryResponse {
    private Long matchId;
    private BigDecimal totalLosingPoolMoney;
    private BigDecimal payoutPerWinner;
    private List<WinnerPayOutInfo> winners;

}
