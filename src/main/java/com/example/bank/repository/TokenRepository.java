package com.example.bank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.bank.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query("""
            SELECT t FROM Token t
            WHERE t.user.id = :userId AND t.loggedOut = false
            """)

    List<Token> findAllAccessTokenByUser(Long userId);

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);
}
