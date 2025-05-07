package com.example.cricket_app.service;

import com.example.cricket_app.dto.request.CreateWalletRequest;
import com.example.cricket_app.dto.request.CreditWalletRequest;
import com.example.cricket_app.dto.response.WalletResponse;

public interface WalletService {


    WalletResponse initializeWallet(CreateWalletRequest request);

    WalletResponse creditWallet(CreditWalletRequest creditWalletRequest);

    WalletResponse viewCurrentBalance();

}
