package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.BetRequest;
import com.example.cricket_app.dto.response.BetResponse;
import com.example.cricket_app.dto.response.PagedBetResponse;
import com.example.cricket_app.entity.Bet;
import com.example.cricket_app.entity.Match;
import com.example.cricket_app.entity.Users;
import com.example.cricket_app.entity.Wallet;
import com.example.cricket_app.enums.BetStatus;
import com.example.cricket_app.enums.MatchStatus;
import com.example.cricket_app.enums.Team;
import com.example.cricket_app.exception.*;
import com.example.cricket_app.mapper.BetMapper;
import com.example.cricket_app.repository.*;
import com.example.cricket_app.security.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BetServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MatchRepository matchRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private BetMapper betMapper;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private WalletTransactionRepository walletTransactionRepository;
    @Mock
    private PayOutRepository payOutRepository;
    @Mock
    private PayOutServiceImpl payOutService;
    @InjectMocks
    private BetServiceImpl betService;

    @Test
    void placeBet_success() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(200));

            Users user = new Users();
            user.setId(1L);

            Match match = new Match();
            match.setId(10L);
            match.setTeamA("INDIA");
            match.setTeamB("PAKISTAN");
            match.setStatus(MatchStatus.UPCOMING);
            match.setBetAmount(BigDecimal.valueOf(100));

            BetRequest request = new BetRequest();
            request.setMatchId(10L);
            request.setTeamChosen(Team.INDIA);

            Bet bet = new Bet();
            bet.setUser(user);
            bet.setMatch(match);
            bet.setTeamChosen(Team.INDIA);
            bet.setAmount(BigDecimal.valueOf(100));
            bet.setStatus(BetStatus.PENDING);
            bet.setCreatedAt(LocalDateTime.now());

            BetResponse expectedResponse = new BetResponse();

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
            when(betRepository.existsByUserAndMatch(user, match)).thenReturn(false);
            when(betRepository.save(any(Bet.class))).thenReturn(bet);
            when(betMapper.toResponse(any(Bet.class))).thenReturn(expectedResponse);

            BetResponse actualResponse = betService.placeBet(request);
            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Test
    void placeBet_walletNotFound_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);
            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.empty());
            BetRequest request = new BetRequest();
            assertThrows(WalletNotFoundException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void placeBet_matchNotFound_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(200));

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(new Users()));
            when(matchRepository.findById(anyLong())).thenReturn(Optional.empty());

            BetRequest request = new BetRequest();
            request.setMatchId(99L);

            assertThrows(MatchNotFoundException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void placeBet_invalidTeamChosen_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(200));

            Users user = new Users();
            Match match = new Match();
            match.setTeamA("INDIA");
            match.setTeamB("PAKISTAN");
            match.setStatus(MatchStatus.UPCOMING);

            BetRequest request = new BetRequest();
            request.setMatchId(1L);
            request.setTeamChosen(Team.AUSTRALIA);

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

            assertThrows(InvalidTeamChosenException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void placeBet_duplicateBet_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(200));

            Users user = new Users();
            Match match = new Match();
            match.setTeamA("INDIA");
            match.setTeamB("PAKISTAN");
            match.setStatus(MatchStatus.UPCOMING);

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(matchRepository.findById(anyLong())).thenReturn(Optional.of(match));
            when(betRepository.existsByUserAndMatch(user, match)).thenReturn(true);

            BetRequest request = new BetRequest();
            request.setMatchId(1L);
            request.setTeamChosen(Team.INDIA);

            assertThrows(DuplicateBetException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void placeBet_ongoingMatch_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(200));

            Users user = new Users();
            Match match = new Match();
            match.setTeamA("INDIA");
            match.setTeamB("PAKISTAN");
            match.setStatus(MatchStatus.ONGOING);

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(matchRepository.findById(anyLong())).thenReturn(Optional.of(match));

            BetRequest request = new BetRequest();
            request.setMatchId(1L);
            request.setTeamChosen(Team.INDIA);

            assertThrows(OngoingMatchException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void placeBet_insufficientBalance_throwsException() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Wallet wallet = new Wallet();
            wallet.setBalance(BigDecimal.valueOf(50));

            Users user = new Users();
            Match match = new Match();
            match.setTeamA("INDIA");
            match.setTeamB("PAKISTAN");
            match.setStatus(MatchStatus.UPCOMING);
            match.setBetAmount(BigDecimal.valueOf(100));

            when(walletRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(wallet));
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(matchRepository.findById(anyLong())).thenReturn(Optional.of(match));

            BetRequest request = new BetRequest();
            request.setMatchId(1L);
            request.setTeamChosen(Team.INDIA);

            assertThrows(InsufficientBalanceException.class, () -> betService.placeBet(request));
        }
    }

    @Test
    void updateBetStatusesForMatchWinner_success() {
        Match match = new Match();
        match.setId(1L);
        match.setWinningTeam(Team.INDIA);

        Bet bet1 = new Bet();
        bet1.setTeamChosen(Team.INDIA);
        Bet bet2 = new Bet();
        bet2.setTeamChosen(Team.PAKISTAN);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(betRepository.findByMatch_Id(1L)).thenReturn(List.of(bet1, bet2));

        betService.updateBetStatusesForMatchWinner(1L);

        assertEquals(BetStatus.WON, bet1.getStatus());
        assertEquals(BetStatus.LOST, bet2.getStatus());
    }

    @Test
    void getUserBetHistory_success() {
        try (MockedStatic<AuthUtils> mockedStatic = mockStatic(AuthUtils.class)) {
            mockedStatic.when(AuthUtils::getLoggedInUserId).thenReturn(1L);

            Bet bet = new Bet();
            BetResponse response = new BetResponse();

            Page<Bet> page = new PageImpl<>(List.of(bet), PageRequest.of(0, 10), 1);

            when(betRepository.findByUser_IdOrderByIdDesc(eq(1L), any())).thenReturn(page);
            when(betMapper.toResponse(any(Bet.class))).thenReturn(response);

            PagedBetResponse paged = betService.getUserBetHistory(1, 10, "createdAt", "DESC");
            assertEquals(1, paged.getTotalPages());
            assertEquals(1, paged.getBets().size());

        }
    }


}
