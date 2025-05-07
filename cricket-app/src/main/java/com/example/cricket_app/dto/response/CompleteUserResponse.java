package com.example.cricket_app.dto.response;

import com.example.cricket_app.enums.UserRole;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CompleteUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;
    private BigDecimal balance;
    private PagedWalletTransactionResponse pagedWalletTransactionResponse;
    private List<BetResponse> bets;
}
