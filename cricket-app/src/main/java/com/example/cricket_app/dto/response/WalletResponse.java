package com.example.cricket_app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class WalletResponse {

    private Long userId;
    private BigDecimal balance;
}
