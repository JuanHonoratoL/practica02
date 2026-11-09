package com.upiiz.practica02.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            SELECT t FROM tokens t INNER JOIN User u ON t.user.id = u.id
            WHERE u.id = :id AND (t.expired = false AND t.revoked = false)
            """)
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
