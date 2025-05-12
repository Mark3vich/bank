package com.example.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bank.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"tokens", "emails", "phones"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.tokens t LEFT JOIN u.emails e WHERE e.email = :email")
    Optional<User> findByEmailWithTokens(@Param("email") String email);

    @Query("SELECT u FROM User u JOIN u.emails e WHERE e.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.emails e WHERE e.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"tokens", "emails", "phones"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.tokens t LEFT JOIN u.phones p WHERE p.phone = :phone")
    Optional<User> findByPhoneWithTokens(@Param("phone") String phone);

    @Query("SELECT u FROM User u JOIN u.phones p WHERE p.phone = :phone")
    Optional<User> findByPhone(@Param("phone") String phone);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM PhoneData p WHERE p.phone = :phone")
    boolean existsByPhone(@Param("phone") String phone);

    Optional<User> findByName(String name);
}
