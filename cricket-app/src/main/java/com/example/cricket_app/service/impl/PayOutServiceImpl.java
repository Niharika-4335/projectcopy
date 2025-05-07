package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.response.PayOutSummaryResponse;
import com.example.cricket_app.dto.response.WinnerPayOutInfo;
import com.example.cricket_app.entity.*;
import com.example.cricket_app.enums.MatchStatus;
import com.example.cricket_app.enums.TransactionType;
import com.example.cricket_app.exception.AdminNotFoundException;
import com.example.cricket_app.exception.MatchNotCompletedException;
import com.example.cricket_app.exception.MatchNotFoundException;
import com.example.cricket_app.exception.WalletNotFoundException;
import com.example.cricket_app.repository.*;
import com.example.cricket_app.service.PayOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayOutServiceImpl implements PayOutService {

    private final BetRepository betRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final PayOutRepository payOutRepository;
    private final MatchRepository matchRepository;

    @Autowired
    public PayOutServiceImpl(BetRepository betRepository,
                             WalletRepository walletRepository,
                             WalletTransactionRepository walletTransactionRepository,
                             PayOutRepository payOutRepository,
                             MatchRepository matchRepository) {
        this.betRepository = betRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.payOutRepository = payOutRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public PayOutSummaryResponse processPayout(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found with ID: " + matchId));

        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new MatchNotCompletedException("Match is not completed.");
        }

        List<Payout> existingPayouts = payOutRepository.findAllByMatch_Id(matchId);
        if (!existingPayouts.isEmpty()) {//if payouts are there for that particular match it will give that summary.
            return getExistingPayoutSummary(existingPayouts, match);
        }

        return createNewPayoutSummary(match);//else it will create new payout summary.
    }

    private PayOutSummaryResponse getExistingPayoutSummary(List<Payout> payouts, Match match) {
        BigDecimal payoutPerUser = payouts.getFirst().getAmount();//as the payout per user is same.so we can use get(0).
        BigDecimal totalLosingPool = payouts.stream()
                .map(Payout::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//(start value,accumulator(sums up the value by adding).

        List<WinnerPayOutInfo> winners = payouts.stream()
                .map(p -> new WinnerPayOutInfo(
                        p.getUser().getId(),
                        p.getUser().getFullName(),
                        payoutPerUser))
                .toList();

        return new PayOutSummaryResponse(match.getId(), totalLosingPool, payoutPerUser, winners);
    }

    private PayOutSummaryResponse createNewPayoutSummary(Match match) {
        List<Bet> allBets = betRepository.findByMatch(match);

        List<Bet> winningBets = allBets.stream()
                .filter(b -> b.getTeamChosen().equals(match.getWinningTeam()))
                .toList();

        List<Bet> losingBets = allBets.stream()
                .filter(b -> !b.getTeamChosen().equals(match.getWinningTeam()))
                .toList();

        if (winningBets.isEmpty()) return handleNoWinners(match, losingBets);
        if (losingBets.isEmpty()) return handleAllWinners(match, winningBets);

        return distributePayout(match, winningBets, losingBets);
    }

    private PayOutSummaryResponse handleNoWinners(Match match, List<Bet> losingBets) {
        BigDecimal totalLosingPool = losingBets.stream()
                .map(Bet::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);//no winners mean admin will get full money.

        Users admin = walletRepository.findAdminUser()
                .orElseThrow(() -> new AdminNotFoundException("Admin not found"));

        Wallet adminWallet = walletRepository.findByUser(admin)
                .orElseThrow(() -> new WalletNotFoundException("Admin wallet not found"));


        adminWallet.setBalance(adminWallet.getBalance().add(totalLosingPool));
        walletRepository.save(adminWallet);

        WalletTransaction adminTxn = new WalletTransaction();
        adminTxn.setWallet(adminWallet);
        adminTxn.setAmount(totalLosingPool);
        adminTxn.setTransactionType(TransactionType.ADMIN_COMMISSION);
        adminTxn.setDescription("Full losing pool taken by admin (no winners) for match " + match.getId());
        adminTxn.setMatch(match);
        walletTransactionRepository.save(adminTxn);

        Payout adminPayout = new Payout();
        adminPayout.setMatch(match);
        adminPayout.setUser(admin);
        adminPayout.setAmount(totalLosingPool);
        payOutRepository.save(adminPayout);

        return new PayOutSummaryResponse(match.getId(), totalLosingPool, BigDecimal.ZERO, List.of());
    }

    private PayOutSummaryResponse handleAllWinners(Match match, List<Bet> winningBets) {
        List<WinnerPayOutInfo> winners = winningBets.stream()//all are winners means zero amount they will get.
                .map(bet -> new WinnerPayOutInfo(
                        bet.getUser().getId(),
                        bet.getUser().getFullName(),
                        BigDecimal.ZERO))
                .toList();

        return new PayOutSummaryResponse(match.getId(), BigDecimal.ZERO, BigDecimal.ZERO, winners);
    }

    //normal use case admin-5% players-remaining
    private PayOutSummaryResponse distributePayout(Match match, List<Bet> winningBets, List<Bet> losingBets) {
        BigDecimal totalLosingPool = losingBets.stream()
                .map(Bet::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal adminCut = totalLosingPool.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.DOWN);
        //3.456->3.45->as we only want two decimal places.
        BigDecimal distributableAmount = totalLosingPool.subtract(adminCut);

        Users admin = walletRepository.findAdminUser()
                .orElseThrow(() -> new AdminNotFoundException("Admin not found"));

        Wallet adminWallet = walletRepository.findByUser(admin)
                .orElseThrow(() -> new WalletNotFoundException("Admin wallet not found"));

        adminWallet.setBalance(adminWallet.getBalance().add(adminCut));
        walletRepository.save(adminWallet);

        WalletTransaction adminTxn = new WalletTransaction();
        adminTxn.setWallet(adminWallet);
        adminTxn.setAmount(adminCut);
        adminTxn.setTransactionType(TransactionType.ADMIN_COMMISSION);
        adminTxn.setDescription("5% commission for match admin" + match.getId());
        adminTxn.setMatch(match);
        walletTransactionRepository.save(adminTxn);

        int numberOfWinners = winningBets.size();
        BigDecimal payoutPerWinner = distributableAmount.divide(
                new BigDecimal(numberOfWinners), 2, RoundingMode.DOWN);

        List<WinnerPayOutInfo> winnerInfos = new ArrayList<>();

        for (Bet bet : winningBets) {
            Users user = bet.getUser();
            Wallet wallet = walletRepository.findByUser(user)
                    .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

            wallet.setBalance(wallet.getBalance().add(payoutPerWinner));
            walletRepository.save(wallet);

            WalletTransaction transaction = new WalletTransaction();
            transaction.setWallet(wallet);
            transaction.setAmount(payoutPerWinner);
            transaction.setTransactionType(TransactionType.WIN_CREDIT);
            transaction.setDescription("Payout for match " + match.getId());
            transaction.setMatch(match);
            walletTransactionRepository.save(transaction);

            Payout payout = new Payout();
            payout.setMatch(match);
            payout.setUser(user);
            payout.setAmount(payoutPerWinner);
            payOutRepository.save(payout);

            winnerInfos.add(new WinnerPayOutInfo(user.getId(), user.getFullName(), payoutPerWinner));
        }

        return new PayOutSummaryResponse(match.getId(), totalLosingPool, payoutPerWinner, winnerInfos);
    }
}

