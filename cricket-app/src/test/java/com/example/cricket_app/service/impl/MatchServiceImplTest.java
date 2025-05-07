package com.example.cricket_app.service.impl;

import com.example.cricket_app.dto.request.CreateMatchRequest;
import com.example.cricket_app.dto.response.MatchResponse;
import com.example.cricket_app.dto.response.PagedUpcomingMatchResponse;
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
import com.example.cricket_app.service.PayOutService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceImplTest {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private MatchMapper matchMapper;
    @Mock
    private
    PastMatchesResultMapper pastMatchesResultMapper;
    @Mock
    private PayOutService payOutService;
    @Mock
    private PayOutMapper payOutMapper;
    @Mock
    private PayOutRepository payOutRepository;
    @Mock
    private BetService betService;
    @Mock
    private BetRepository betRepository;


    @InjectMocks
    private MatchServiceImpl matchService;

    @Test
    void testCreateMatch_success() {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setTeamA("INDIA");
        request.setTeamB("AUSTRALIA");
        request.setStartTime(LocalDateTime.now().plusMinutes(10));

        Match match = new Match();
        Match savedMatch = new Match();
        MatchResponse response = new MatchResponse();

        when(matchMapper.toEntity(request)).thenReturn(match);
        when(matchRepository.save(match)).thenReturn(savedMatch);
        when(matchMapper.toResponseDto(savedMatch)).thenReturn(response);

        MatchResponse result = matchService.createMatch(request);
        assertEquals(response, result);
    }

    @Test
    void testCreateMatch_sameTeams_shouldThrow() {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setTeamA("INDIA");
        request.setTeamB("INDIA");
        request.setStartTime(LocalDateTime.now().plusMinutes(10));

        assertThrows(SameTeamSelectionException.class, () -> {
            matchService.createMatch(request);
        });
    }

    @Test
    void testCreateMatch_startTimeInPast_shouldThrow() {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setTeamA("INDIA");
        request.setTeamB("AUSTRALIA");
        request.setStartTime(LocalDateTime.now().minusMinutes(10));

        assertThrows(MatchStartTimeInPastException.class, () -> {
            matchService.createMatch(request);
        });
    }

    @Test
    void testDeclareMatchWinner_success() {
        Long matchId = 1L;
        String winningTeam = "INDIA";
        Match match = new Match();
        match.setTeamA("INDIA");
        match.setTeamB("AUSTRALIA");
        match.setStartTime(LocalDateTime.now().minusMinutes(30));

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(payOutRepository.findAllByMatch_Id(matchId)).thenReturn(List.of(new Payout()));
        when(payOutMapper.toResponse(any())).thenReturn(new PayoutResponse());

        List<PayoutResponse> result = matchService.declareMatchWinner(matchId, winningTeam);
        assertEquals(1, result.size());
        verify(matchRepository).save(match);
    }

    @Test
    void testDeclareMatchWinner_invalidTeam_shouldThrow() {
        Match match = new Match();
        match.setTeamA("INDIA");
        match.setTeamB("AUSTRALIA");
        match.setStartTime(LocalDateTime.now().minusMinutes(30));
        Long matchId = 1L;

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThrows(InvalidTeamChosenException.class, () ->
                matchService.declareMatchWinner(matchId, "ENGLAND")
        );
    }

    @Test
    void testDeclareMatchWinner_alreadyDeclared_shouldThrow() {
        Long matchId = 1L;
        String winningTeam = "INDIA";
        Match match = new Match();
        match.setTeamA("INDIA");
        match.setTeamB("AUSTRALIA");
        match.setStartTime(LocalDateTime.now().minusMinutes(30));
        match.setWinningTeam(Team.INDIA);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThrows(WinnerAlreadyDeclaredException.class, () ->
                matchService.declareMatchWinner(matchId, winningTeam)
        );
    }

    @Test
    void testDeclareMatchWinner_notStarted_shouldThrow() {
        Long matchId = 1L;
        String winningTeam = "INDIA";
        Match match = new Match();
        match.setTeamA("INDIA");
        match.setTeamB("AUSTRALIA");
        match.setStartTime(LocalDateTime.now().plusMinutes(10)); // Match has not started

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        assertThrows(MatchNotStartedException.class, () ->
                matchService.declareMatchWinner(matchId, winningTeam)
        );
    }

    @Test
    void testDeclareMatchWinner_noPayouts_shouldThrow() {
        Long matchId = 1L;
        String winningTeam = "INDIA";
        Match match = new Match();
        match.setTeamA("INDIA");
        match.setTeamB("AUSTRALIA");
        match.setStartTime(LocalDateTime.now().minusMinutes(30));

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(payOutRepository.findAllByMatch_Id(matchId)).thenReturn(List.of());

        assertThrows(PayoutNotFoundException.class, () ->
                matchService.declareMatchWinner(matchId, winningTeam)
        );
    }

    @Test
    void testCreateMatch_withInvalidTeamName_throwsInvalidTeamChosenException() {
        CreateMatchRequest request = new CreateMatchRequest();
        request.setTeamA("InvalidTeam");
        request.setTeamB("India");
        request.setStartTime(LocalDateTime.now().plusHours(1));

        assertThrows(InvalidTeamChosenException.class, () -> matchService.createMatch(request));
    }

    @Test
    void testViewPastMatchesResults_returnsList() {
        Match completedMatch = new Match();
        completedMatch.setStatus(MatchStatus.COMPLETED);
        when(matchRepository.findAll()).thenReturn(List.of(completedMatch));
        when(pastMatchesResultMapper.toResponseDtoList(any())).thenReturn(List.of());

        assertDoesNotThrow(() -> matchService.viewPastMatchesResults());
        verify(pastMatchesResultMapper).toResponseDtoList(List.of(completedMatch));
    }

    @Test
    void testUpdateMatchStatuses_shouldUpdateStatusCorrectly() {
        Match upcoming = new Match();
        upcoming.setStatus(MatchStatus.UPCOMING);
        upcoming.setStartTime(LocalDateTime.now().minusMinutes(1));

        Match ongoing = new Match();
        ongoing.setStatus(MatchStatus.ONGOING);
        ongoing.setStartTime(LocalDateTime.now().minusMinutes(25));

        when(matchRepository.findAll()).thenReturn(List.of(upcoming, ongoing));
        when(betRepository.existsByMatch(ongoing)).thenReturn(false);

        matchService.updateMatchStatuses();

        verify(matchRepository, times(2)).save(any());
    }

    @Test
    void testGetUpcomingMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Match match1 = new Match();
        Match match2 = new Match();
        List<Match> matches = Arrays.asList(match1, match2);
        Page<Match> matchPage = new PageImpl<>(matches, pageable, 2);

        when(matchRepository.findUpcomingMatches(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(matchPage);

        MatchResponse matchResponse1 = new MatchResponse();
        MatchResponse matchResponse2 = new MatchResponse();
        when(matchMapper.toResponseDto(match1)).thenReturn(matchResponse1);
        when(matchMapper.toResponseDto(match2)).thenReturn(matchResponse2);

        PagedUpcomingMatchResponse response = matchService.getUpcomingMatches(pageable);

        assertNotNull(response);
        assertEquals(2, response.getMatches().size());
        assertEquals(1, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(matchResponse1, response.getMatches().get(0));
        assertEquals(matchResponse2, response.getMatches().get(1)); // Ensure second match matches the response
    }

}

