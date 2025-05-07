package com.example.cricket_app.repository;

import com.example.cricket_app.entity.Match;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE m.status = 'UPCOMING' AND m.startTime > :now ORDER BY m.startTime ASC")
    Page<Match> findUpcomingMatches(LocalDateTime now, Pageable pageable);//writing native jpql query.


}
