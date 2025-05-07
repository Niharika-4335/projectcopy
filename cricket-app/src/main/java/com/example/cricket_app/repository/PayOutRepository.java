package com.example.cricket_app.repository;

import com.example.cricket_app.entity.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayOutRepository extends JpaRepository<Payout, Long> {

    List<Payout> findAllByMatch_Id(Long id);
    //here in payout entity match is there we are getting id.

}
