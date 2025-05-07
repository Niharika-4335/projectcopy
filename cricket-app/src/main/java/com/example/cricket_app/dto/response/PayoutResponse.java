package com.example.cricket_app.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayoutResponse {
    BigDecimal payoutPerWinner;
    @Schema(type = "string", format = "date-time", example = "2025-04-30T04:54:29.709Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime processedAt;
    Integer matchId;
    Integer userId;
}
