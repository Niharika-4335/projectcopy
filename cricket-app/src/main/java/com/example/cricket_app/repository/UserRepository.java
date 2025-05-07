package com.example.cricket_app.repository;

import com.example.cricket_app.entity.Users;
import com.example.cricket_app.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByEmail(String email);

    Optional<Users> findByEmail(String email);

    Page<Users> findByRole(UserRole role, Pageable pageable);


}
