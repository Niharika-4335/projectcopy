package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.response.PayOutSummaryResponse;
import com.example.cricket_app.entity.*;
import com.example.cricket_app.enums.MatchStatus;
import com.example.cricket_app.enums.Team;
import com.example.cricket_app.exception.MatchNotCompletedException;
import com.example.cricket_app.exception.MatchNotFoundException;
import com.example.cricket_app.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayOutServiceImplTest {

    @InjectMocks
    private PayOutServiceImpl payOutService;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private PayOutRepository payOutRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Test
    void testProcessPayout_existingPayouts_returnsSummary() {
        Long matchId = 1L;
        Match match = new Match();
        match.setId(matchId);
        match.setStatus(MatchStatus.COMPLETED);

        Users user = new Users();
        user.setId(10L);
        user.setFullName("Winner One");

        Payout payout = new Payout();
        payout.setUser(user);
        payout.setAmount(new BigDecimal("100.00"));
        payout.setMatch(match);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(payOutRepository.findAllByMatch_Id(matchId)).thenReturn(List.of(payout));
        PayOutSummaryResponse result = payOutService.processPayout(matchId);

        assertEquals(matchId, result.getMatchId());
        assertEquals(new BigDecimal("100.00"), result.getTotalLosingPoolMoney());
        assertEquals(1, result.getWinners().size());
        assertEquals("Winner One", result.getWinners().getFirst().getUsername());
    }

    @Test
    void testProcessPayout_matchNotFound_throwsException() {
        when(matchRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(MatchNotFoundException.class, () -> payOutService.processPayout(1L));
    }

    @Test
    void testProcessPayout_matchNotCompleted_throwsException() {
        Match match = new Match();
        match.setId(1L);
        match.setStatus(MatchStatus.UPCOMING);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        assertThrows(MatchNotCompletedException.class, () -> payOutService.processPayout(1L));
    }

    @Test
    void testProcessPayout_noWinners_adminGetsFullAmount() {
        Long matchId = 1L;

        Match match = new Match();
        match.setId(matchId);
        match.setStatus(MatchStatus.COMPLETED);
        match.setWinningTeam(Team.AUSTRALIA);


        Bet bet1 = new Bet();
        bet1.setAmount(new BigDecimal("200.00"));
        bet1.setTeamChosen(Team.INDIA);

        Users adminUser = new Users();
        adminUser.setId(99L);
        adminUser.setFullName("Admin");

        Wallet adminWallet = new Wallet();
        adminWallet.setUser(adminUser);
        adminWallet.setBalance(new BigDecimal("1000.00"));

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(betRepository.findByMatch(match)).thenReturn(List.of(bet1));
        when(walletRepository.findAdminUser()).thenReturn(Optional.of(adminUser));
        when(walletRepository.findByUser(adminUser)).thenReturn(Optional.of(adminWallet));
        when(walletRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(walletTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(payOutRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(payOutRepository.findAllByMatch_Id(matchId)).thenReturn(List.of());

        PayOutSummaryResponse response = payOutService.processPayout(matchId);


        assertEquals(new BigDecimal("1200.00"), adminWallet.getBalance());
        assertEquals(new BigDecimal("200.00"), response.getTotalLosingPoolMoney());
        assertEquals(BigDecimal.ZERO, response.getPayoutPerWinner());
    }

}
