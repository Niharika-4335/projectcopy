package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.response.PagedWalletTransactionResponse;
import com.example.cricket_app.dto.response.WalletTransactionResponse;
import com.example.cricket_app.entity.WalletTransaction;
import com.example.cricket_app.enums.TransactionType;
import com.example.cricket_app.mapper.WalletTransactionMapper;
import com.example.cricket_app.repository.WalletTransactionRepository;
import com.example.cricket_app.security.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class WalletTransactionImplTest {
    @InjectMocks
    private WalletTransactionImpl walletTransactionService;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private WalletTransactionMapper walletTransactionMapper;

    @Test
    void getTransactionsByUserId_shouldReturnPagedResponse() {
        int page = 1;
        int size = 2;
        String sortBy = "createdAt";
        String direction = "DESC";
        Long userId = 1L;

        // Create WalletTransaction objects
        WalletTransaction tx1 = new WalletTransaction();
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setTransactionType(TransactionType.WIN_CREDIT);
        tx1.setDescription("Test");
        tx1.setCreatedAt(LocalDateTime.now());

        WalletTransaction tx2 = new WalletTransaction();
        tx2.setAmount(BigDecimal.valueOf(200));
        tx2.setTransactionType(TransactionType.BET_PLACED);
        tx2.setDescription("Test 2");
        tx2.setCreatedAt(LocalDateTime.now());

        // Mock the repository call with Page result
        List<WalletTransaction> transactionList = List.of(tx1, tx2);
        Page<WalletTransaction> pageResult = new PageImpl<>(transactionList, PageRequest.of(0, size), 5);

        // Create corresponding response DTOs
        WalletTransactionResponse dto1 = new WalletTransactionResponse();
        dto1.setAmount(tx1.getAmount());

        WalletTransactionResponse dto2 = new WalletTransactionResponse();
        dto2.setAmount(tx2.getAmount());

        try (MockedStatic<AuthUtils> mockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            // Mock static method AuthUtils.getLoggedInUserId
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(userId);

            // Mock repository and mapper methods
            Mockito.when(walletTransactionRepository.findByWallet_User_IdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                    .thenReturn(pageResult);
            Mockito.when(walletTransactionMapper.toResponseDto(tx1)).thenReturn(dto1);
            Mockito.when(walletTransactionMapper.toResponseDto(tx2)).thenReturn(dto2);

            // Act
            PagedWalletTransactionResponse result = walletTransactionService.getTransactionsByUserId(page, size, sortBy, direction);

            // Assert
            assertEquals(1, result.getCurrentPage());
            assertEquals(3, result.getTotalPages());
            assertEquals(5, result.getTotalUsers());
            assertEquals(2, result.getWalletTransactionResponses().size());
            assertEquals(BigDecimal.valueOf(100), result.getWalletTransactionResponses().get(0).getAmount());
        }
    }
}
