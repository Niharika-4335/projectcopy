package com.example.cricket_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WinnerPayOutInfo {
    private Long userId;
    private String username;
    private BigDecimal amountCredited;
}
