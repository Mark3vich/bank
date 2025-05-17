package com.example.bank.service;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bank.model.User;
import com.example.bank.repository.TokenRepository;
import com.example.bank.repository.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public JwtService(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean isValid(String token, UserDetails userDetails) {
        try {
            String identifier = extractIdentifier(token);
            User user = getUserByIdentifier(identifier);

            return isTokenActive(token)
                    && isIdentifierValid(identifier, user)
                    && !isTokenExpired(token);
        } catch (Exception e) {
            logError("Token validation error: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean isValidRefresh(String token, User user) {
        try {
            String identifierFromToken = extractIdentifier(token);
            boolean isTokenValid = isRefreshTokenActive(token);

            return isIdentifierValid(identifierFromToken, user)
                    && isTokenValid
                    && !isTokenExpired(token);
        } catch (Exception e) {
            logError("Refresh token validation error: " + e.getMessage());
            return false;
        } 
    }

    @Transactional
    private User getUserByIdentifier(String identifier) {
        // Сначала проверяем email, потом телефон
        if (identifier.contains("@")) {
            return userRepository.findByEmailWithTokens(identifier)
                    .orElseThrow(() -> {
                        logError("User not found with email: " + identifier);
                        return new UsernameNotFoundException("User not found");
                    });
        } else {
            return userRepository.findByPhoneWithTokens(identifier)
                    .orElseThrow(() -> {
                        logError("User not found with phone: " + identifier);
                        return new UsernameNotFoundException("User not found");
                    });
        }
    }

    @Transactional
    private boolean isIdentifierValid(String identifier, User user) {
        // Проверяем email
        boolean emailMatch = user.getEmails().stream()
                .anyMatch(email -> email.getEmail().equals(identifier));

        // Если не нашли по email, проверяем телефон
        if (!emailMatch) {
            boolean phoneMatch = user.getPhones().stream()
                    .anyMatch(phone -> phone.getPhone().equals(identifier));
            if (!phoneMatch) {
                logError("Identifier mismatch: token identifier=" + identifier);
                return false;
            }
        }
        return true;
    }

    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenActive(String token) {
        return tokenRepository.findByAccessToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);
    }

    private boolean isRefreshTokenActive(String token) {
        return tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut())
                .orElse(false);
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logError("Token expiration check error: " + e.getMessage());
            return true;
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpiration);
    }

    private String generateToken(User user, long expiryTime) {
        String identifier = user.getEmails().isEmpty()
                ? ""
                : user.getEmails().iterator().next().getEmail();

        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser()
                .verifyWith(getSigningKey());
        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private void logError(String message) {
        logger.error(message);
        System.err.println("[" + new Date() + "] ERROR: " + message);
    }
}
