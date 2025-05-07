package com.example.cricket_app.dto.request;

import com.example.cricket_app.enums.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BetRequest {
    @NotNull(message = "Match ID is required")
    private Long matchId;
    @NotBlank(message = "team should not be null")
    private Team teamChosen;
}
