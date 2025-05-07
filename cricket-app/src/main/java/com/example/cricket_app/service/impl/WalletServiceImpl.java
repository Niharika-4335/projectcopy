package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateWalletRequest;
import com.example.cricket_app.dto.request.CreditWalletRequest;
import com.example.cricket_app.dto.response.WalletResponse;
import com.example.cricket_app.entity.Users;
import com.example.cricket_app.entity.Wallet;
import com.example.cricket_app.entity.WalletTransaction;
import com.example.cricket_app.enums.TransactionType;
import com.example.cricket_app.exception.NonPositiveAmountException;
import com.example.cricket_app.exception.UserNotFoundException;
import com.example.cricket_app.exception.WalletNotFoundException;
import com.example.cricket_app.mapper.WalletMapper;
import com.example.cricket_app.repository.UserRepository;
import com.example.cricket_app.repository.WalletRepository;
import com.example.cricket_app.repository.WalletTransactionRepository;
import com.example.cricket_app.security.AuthUtils;
import com.example.cricket_app.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

    private static final String ACTION = "User not found";
    private final WalletMapper walletMapper;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;


    @Autowired
    public WalletServiceImpl(WalletMapper walletMapper, WalletRepository walletRepository, UserRepository userRepository, WalletTransactionRepository walletTransactionRepository) {
        this.walletMapper = walletMapper;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;

    }

    @Override
    public WalletResponse initializeWallet(CreateWalletRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ACTION));

        //if wallet is already present we are getting it by using wallet repository method.
        if (walletRepository.findByUser(user).isPresent()) {
            return walletMapper.toResponseDto(walletRepository.findByUser(user).get());
        }

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet savedWallet = walletRepository.save(wallet);

        return walletMapper.toResponseDto(savedWallet);
    }

    @Override
    public WalletResponse creditWallet(CreditWalletRequest creditWalletRequest) {
        if (creditWalletRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NonPositiveAmountException("Amount must be greater than 0");
        }

        Users user = userRepository.findById(creditWalletRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ACTION));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setCreatedAt(LocalDateTime.now());
                    newWallet.setUpdatedAt(LocalDateTime.now());
                    return walletRepository.save(newWallet);
                });

        wallet.setBalance(wallet.getBalance().add(creditWalletRequest.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(creditWalletRequest.getAmount());
        transaction.setTransactionType(TransactionType.ADMIN_CREDIT);
        transaction.setDescription(creditWalletRequest.getDescription());
        transaction.setMatch(null); // Admin credit doesn't need a match
        walletTransactionRepository.save(transaction);

        return walletMapper.toResponseDto(wallet);
    }

    @Override
    public WalletResponse viewCurrentBalance() {
        Long userId = AuthUtils.getLoggedInUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ACTION));

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        return walletMapper.toResponseDto(wallet);
    }


}
