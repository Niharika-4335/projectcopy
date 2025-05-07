package com.example.cricket_app.controller;


import com.example.cricket_app.dto.request.CreditWalletRequest;
import com.example.cricket_app.dto.response.WalletResponse;
import com.example.cricket_app.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;

    @Autowired
    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/credit")
    public ResponseEntity<WalletResponse> creditWallet(@Valid @RequestBody CreditWalletRequest request) {
        WalletResponse response = walletService.creditWallet(request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PLAYER')")
    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> viewCurrentBalance() {
        WalletResponse response = walletService.viewCurrentBalance();
        return ResponseEntity.ok(response);
    }


}
