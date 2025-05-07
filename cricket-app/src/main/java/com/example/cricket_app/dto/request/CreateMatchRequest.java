package com.example.cricket_app.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateMatchRequest {
    @NotBlank(message = "Team A name is required")
    private String teamA;

    @NotBlank(message = "Team B name is required")
    private String teamB;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")//start time should be in the future.
    private LocalDateTime startTime;

    @NotNull(message = "Bet amount is required")
    @Positive(message = "Bet amount must be positive")
    private BigDecimal betAmount;

}
