package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.BetRequest;
import com.example.cricket_app.dto.response.BetResponse;
import com.example.cricket_app.dto.response.PagedBetResponse;
import com.example.cricket_app.entity.*;
import com.example.cricket_app.enums.BetStatus;
import com.example.cricket_app.enums.MatchStatus;
import com.example.cricket_app.enums.Team;
import com.example.cricket_app.enums.TransactionType;
import com.example.cricket_app.exception.*;
import com.example.cricket_app.mapper.BetMapper;
import com.example.cricket_app.repository.*;
import com.example.cricket_app.security.AuthUtils;
import com.example.cricket_app.service.BetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BetServiceImpl implements BetService {
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final BetRepository betRepository;
    private final BetMapper betMapper;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    @Autowired
    public BetServiceImpl(UserRepository userRepository, MatchRepository matchRepository, BetRepository betRepository, BetMapper betMapper, WalletRepository walletRepository, WalletTransactionRepository walletTransactionRepository) {
        this.userRepository = userRepository;
        this.matchRepository = matchRepository;
        this.betRepository = betRepository;
        this.betMapper = betMapper;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
//    @Transactional
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BetResponse placeBet(BetRequest request) {
        Long userId = AuthUtils.getLoggedInUserId();
        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user " + userId));

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Match match = matchRepository.findById(request.getMatchId())
                .orElseThrow(() -> new MatchNotFoundException("Match not found"));

        String teamChosen = String.valueOf(request.getTeamChosen());
        String teamA = match.getTeamA();
        String teamB = match.getTeamB();
        if (!teamChosen.equalsIgnoreCase(teamA) && !teamChosen.equalsIgnoreCase(teamB)) {
            throw new InvalidTeamChosenException("Chosen team is not participating in this match.");
        }

        if (betRepository.existsByUserAndMatch(user, match)) {
            throw new DuplicateBetException("User already placed a bet for this match.");
        }

        if (match.getStatus() == MatchStatus.ONGOING || match.getStatus() == MatchStatus.COMPLETED) {
            throw new OngoingMatchException("Bets are not allowed after the match has started.");
        }
        BigDecimal betAmount = match.getBetAmount();
        BigDecimal remainingBalance = debitMoneyFromWallet(wallet, betAmount);
        wallet.setBalance(remainingBalance);
        walletRepository.save(wallet);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setWallet(wallet);
        transaction.setAmount(betAmount);
        transaction.setTransactionType(TransactionType.BET_PLACED);
        transaction.setDescription("Bet placed on match " + match.getId());
        transaction.setMatch(match);
        walletTransactionRepository.save(transaction);
        Bet bet = new Bet();
        bet.setUser(user);
        bet.setMatch(match);
        bet.setTeamChosen(Team.valueOf(teamChosen));
        bet.setAmount(betAmount);
        bet.setStatus(BetStatus.PENDING);
        bet.setCreatedAt(LocalDateTime.now());
        betRepository.save(bet);
        return betMapper.toResponse(bet);

    }

    private static BigDecimal debitMoneyFromWallet(Wallet wallet, BigDecimal betAmount) {
        BigDecimal currentBalance = wallet.getBalance();
        if (currentBalance == null || currentBalance.compareTo(betAmount) < 0) {
            throw new InsufficientBalanceException("Not enough funds to place this bet.");
        }
        return currentBalance.subtract(betAmount);
    }


    @Override
    public void updateBetStatusesForMatchWinner(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found"));

        if (match.getWinningTeam() == null) {
            throw new MatchWinnerNotDeclaredException("Match winner is not declared yet.");
        }

        List<Bet> bets = betRepository.findByMatch_Id(matchId);

        for (Bet bet : bets) {
            if (bet.getTeamChosen().equals(match.getWinningTeam())) {
                bet.setStatus(BetStatus.WON);
            } else {
                bet.setStatus(BetStatus.LOST);
            }
        }

        betRepository.saveAll(bets);
    }

    @Override
    public PagedBetResponse getUserBetHistory(int page, int size, String sortBy, String direction) {
        int pageNumber = Math.max(0, page - 1);
        int pageSize = Math.max(1, size);
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(sortDirection, sortBy));
        Long userId = AuthUtils.getLoggedInUserId();
        Page<Bet> bets = betRepository.findByUser_IdOrderByIdDesc(userId, pageable);
        List<BetResponse> betResponseList = bets.map(betMapper::toResponse).getContent();
        //betMapper is an object and toResponse refers to the instance method
        return new PagedBetResponse(
                betResponseList,
                bets.getNumber() + 1,
                bets.getTotalPages(),
                bets.getTotalElements()
        );
    }
}
