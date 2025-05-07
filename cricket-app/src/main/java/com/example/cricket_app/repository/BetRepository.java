package com.example.cricket_app.repository;

import com.example.cricket_app.entity.Bet;
import com.example.cricket_app.entity.Match;
import com.example.cricket_app.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    boolean existsByUserAndMatch(Users user, Match match);//if there exists  a match with particular user.

    List<Bet> findByUser_IdOrderByIdDesc(Long userId);
    //here performing nested query so using underscore.
    //Bet we have user as field we want bet id so we use underscore while writing nested queries.

    List<Bet> findByMatch(Match match);

    List<Bet> findByMatch_Id(Long matchId);


    Page<Bet> findByUser_IdOrderByIdDesc(Long userId, Pageable pageable);

    boolean existsByMatch(Match match);

}
