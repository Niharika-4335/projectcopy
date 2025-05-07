package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateMatchRequest;
import com.example.cricket_app.dto.response.MatchResponse;
import com.example.cricket_app.dto.response.PagedUpcomingMatchResponse;
import com.example.cricket_app.dto.response.PastMatchesResultResponse;
import com.example.cricket_app.dto.response.PayoutResponse;
import com.example.cricket_app.entity.Match;
import com.example.cricket_app.entity.Payout;
import com.example.cricket_app.enums.MatchStatus;
import com.example.cricket_app.enums.Team;
import com.example.cricket_app.exception.*;
import com.example.cricket_app.mapper.MatchMapper;
import com.example.cricket_app.mapper.PastMatchesResultMapper;
import com.example.cricket_app.mapper.PayOutMapper;
import com.example.cricket_app.repository.BetRepository;
import com.example.cricket_app.repository.MatchRepository;
import com.example.cricket_app.repository.PayOutRepository;
import com.example.cricket_app.service.BetService;
import com.example.cricket_app.service.MatchService;
import com.example.cricket_app.service.PayOutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {
    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final PayOutService payOutService;
    private final PastMatchesResultMapper pastMatchesResultMapper;
    private final BetService betService;
    private final PayOutMapper payOutMapper;
    private final PayOutRepository payOutRepository;
    private final BetRepository betRepository;

    @Autowired
    public MatchServiceImpl(MatchRepository matchRepository, MatchMapper matchMapper, PayOutService payOutService, PastMatchesResultMapper pastMatchesResultMapper, BetService betService, PayOutMapper payOutMapper, PayOutRepository payOutRepository, BetRepository betRepository) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.payOutService = payOutService;
        this.pastMatchesResultMapper = pastMatchesResultMapper;
        this.betService = betService;
        this.payOutMapper = payOutMapper;
        this.payOutRepository = payOutRepository;
        this.betRepository = betRepository;
    }

    @Override
    public MatchResponse createMatch(CreateMatchRequest createMatchRequest) {
        //getting two teams names and checking whether they are in our enum or not.
        try {
            Team.valueOf(createMatchRequest.getTeamA().toUpperCase());
            Team.valueOf(createMatchRequest.getTeamB().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidTeamChosenException("Invalid team name. Must be one of: " + Arrays.toString(Team.values()));
        }

        if (createMatchRequest.getTeamA().equalsIgnoreCase(createMatchRequest.getTeamB())) {
            throw new SameTeamSelectionException("Teams cannot be the same");
        }

        // if match start time is before(passed time) the current time.
        if (createMatchRequest.getStartTime().isBefore(LocalDateTime.now())) {
            throw new MatchStartTimeInPastException("Match start time must be in the future");
        }

        Match match = matchMapper.toEntity(createMatchRequest);
        Match savedMatch = matchRepository.save(match);

        return matchMapper.toResponseDto(savedMatch);

    }

    @Override
    public PagedUpcomingMatchResponse getUpcomingMatches(Pageable pageable) {
        Page<Match> matches = matchRepository.findUpcomingMatches(LocalDateTime.now(), pageable);
        List<MatchResponse> matchResponses = matches.getContent()//we use get content to retrieve data from page.
                .stream()
                .map(matchMapper::toResponseDto)
                .toList();

        return new PagedUpcomingMatchResponse(
                matchResponses,
                matches.getNumber() + 1,
                matches.getTotalPages(),
                matches.getTotalElements()
        );

    }

    @Override
    public List<PayoutResponse> declareMatchWinner(Long matchId, String winningTeam) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException("Match not found"));

        if (match.getWinningTeam() != null) {
            throw new WinnerAlreadyDeclaredException("Winner already declared for this match.");
        }

        if (match.getStartTime().isAfter(LocalDateTime.now())) {
            throw new MatchNotStartedException("Cannot declare winner before match starts.");
        }
        Team winningTeamEnum = Team.valueOf(winningTeam);//valueOf converts String to Enum

        if (!(match.getTeamA().equalsIgnoreCase(winningTeam) ||
                match.getTeamB().equalsIgnoreCase(winningTeam))) {
            throw new InvalidTeamChosenException("Winning team must be one of the teams that played the match.");
        }


        match.setWinningTeam(winningTeamEnum);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);
        betService.updateBetStatusesForMatchWinner(matchId);
        payOutService.processPayout(matchId);
        List<Payout> payouts = payOutRepository.findAllByMatch_Id(matchId);

        if (payouts.isEmpty()) {
            throw new PayoutNotFoundException("No payouts found after processing payout!");
        }

        return payouts.stream()
                .map(payOutMapper::toResponse)
                .toList();//we are returning payouts response after match is created.
    }

    @Override
    public List<PastMatchesResultResponse> viewPastMatchesResults() {
        List<Match> matches = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.COMPLETED).toList();

        return pastMatchesResultMapper.toResponseDtoList(matches);//returning completed matches as lists
    }


    @Scheduled(fixedRate = 60000)//in milliseconds=1 min
    public void updateMatchStatuses() {
        LocalDateTime now = LocalDateTime.now();
        // Find matches that are Upcoming but should be Ongoing.
        List<Match> matches = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.UPCOMING && m.getStartTime().isBefore(now))
                .toList();

        for (Match match : matches) {
            match.setStatus(MatchStatus.ONGOING);
            match.setUpdatedAt(now);
            matchRepository.save(match);
        }//to change the status if time passed from the present time now.

        List<Match> toComplete = matchRepository.findAll().stream()
                .filter(m -> m.getStatus() == MatchStatus.ONGOING && m.getStartTime().plusMinutes(20).isBefore(now))
                .toList();

        for (Match match : toComplete) {
            boolean betsExist = betRepository.existsByMatch(match);
            if (!betsExist) {
                match.setStatus(MatchStatus.AUTO_COMPLETED);
                match.setUpdatedAt(now);
                matchRepository.save(match);
            }

        }
        //after 20 minutes...match will be completed even if we place bets or not.match  will get auto completed.

    }


}
