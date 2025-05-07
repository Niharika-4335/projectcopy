package com.example.cricket_app.dto.response;

import com.example.cricket_app.enums.MatchStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MatchResponse {
    private Long matchId;
    private String teamA;
    private String teamB;
    @Schema(type = "string", format = "date-time", example = "2025-04-30T04:54:29.709Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime startTime;
    private BigDecimal betAmount;
    private MatchStatus status;


}
