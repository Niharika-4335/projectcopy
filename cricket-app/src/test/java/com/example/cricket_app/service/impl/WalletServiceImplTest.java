package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateWalletRequest;
import com.example.cricket_app.dto.request.CreditWalletRequest;
import com.example.cricket_app.dto.response.WalletResponse;
import com.example.cricket_app.entity.Users;
import com.example.cricket_app.entity.Wallet;
import com.example.cricket_app.exception.NonPositiveAmountException;
import com.example.cricket_app.exception.UserNotFoundException;
import com.example.cricket_app.exception.WalletNotFoundException;
import com.example.cricket_app.mapper.WalletMapper;
import com.example.cricket_app.repository.UserRepository;
import com.example.cricket_app.repository.WalletRepository;
import com.example.cricket_app.repository.WalletTransactionRepository;
import com.example.cricket_app.security.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void initializeWallet() {
        Long userId = 1L;
        Users user = new Users();
        user.setId(userId);

        CreateWalletRequest request = new CreateWalletRequest();
        request.setUserId(userId);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        WalletResponse walletResponse = new WalletResponse(userId, BigDecimal.ZERO);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());
        when(walletRepository.save(any())).thenReturn(wallet);
        when(walletMapper.toResponseDto(wallet)).thenReturn(walletResponse);

        WalletResponse result = walletService.initializeWallet(request);

        assertNotNull(result);
        assertEquals(walletResponse.getUserId(), result.getUserId());

        verify(walletRepository).save(any());
    }

    @Test
    void creditWallet() {
        Long userId = 1L;
        CreditWalletRequest creditRequest = new CreditWalletRequest();
        creditRequest.setUserId(userId);
        creditRequest.setAmount(BigDecimal.valueOf(100));
        creditRequest.setDescription("Admin deposit");

        Users user = new Users();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        WalletResponse expectedResponse = new WalletResponse(userId, BigDecimal.valueOf(100));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(walletMapper.toResponseDto(wallet)).thenReturn(expectedResponse);

        WalletResponse response = walletService.creditWallet(creditRequest);

        assertEquals(BigDecimal.valueOf(100), response.getBalance());
        verify(walletTransactionRepository).save(any());
    }

    @Test
    void testCreditWallet_ThrowsNonPositiveAmount() {
        CreditWalletRequest creditRequest = new CreditWalletRequest();
        creditRequest.setAmount(BigDecimal.ZERO);

        assertThrows(NonPositiveAmountException.class,
                () -> walletService.creditWallet(creditRequest));
    }

    @Test
    void viewCurrentBalance_Success() {
        Long userId = 1L;
        Users user = new Users();
        user.setId(userId);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.valueOf(500));

        WalletResponse expectedResponse = new WalletResponse(userId, BigDecimal.valueOf(500));

        try (MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class)) {
            mockedAuthUtils.when(AuthUtils::getLoggedInUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
            when(walletMapper.toResponseDto(wallet)).thenReturn(expectedResponse);

            WalletResponse result = walletService.viewCurrentBalance();

            assertEquals(BigDecimal.valueOf(500), result.getBalance());
        }
    }

    @Test
    void testViewCurrentBalance_UserNotFound() {
        Long userId = 1L;

        try (MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class)) {
            mockedAuthUtils.when(AuthUtils::getLoggedInUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> walletService.viewCurrentBalance());
        }
    }

    @Test
    void testViewCurrentBalance_WalletNotFound() {
        Long userId = 1L;
        Users user = new Users();
        user.setId(userId);

        try (MockedStatic<AuthUtils> mockedAuthUtils = mockStatic(AuthUtils.class)) {
            mockedAuthUtils.when(AuthUtils::getLoggedInUserId).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

            assertThrows(WalletNotFoundException.class, () -> walletService.viewCurrentBalance());
        }
    }
}
