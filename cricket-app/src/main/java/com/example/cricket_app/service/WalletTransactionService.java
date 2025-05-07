package com.example.cricket_app.service;

import com.example.cricket_app.dto.response.PagedWalletTransactionResponse;

public interface WalletTransactionService {

    PagedWalletTransactionResponse getTransactionsByUserId(int page, int size, String sortBy, String direction);
}