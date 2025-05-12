package com.example.bank.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bank.dto.request.AuthenticationRequest;
import com.example.bank.dto.request.RefreshTokenRequest;
import com.example.bank.dto.response.AuthenticationResponse;
import com.example.bank.model.Account;
import com.example.bank.model.Token;
import com.example.bank.model.User;
import com.example.bank.repository.TokenRepository;
import com.example.bank.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
@CacheConfig(cacheNames = "tokens")
public class AuthenticationService {

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final TokenRepository tokenRepository;
    
    private final InterestService interestService;

    public AuthenticationService(UserRepository userRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenRepository tokenRepository,
            InterestService interestService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.interestService = interestService;
    }

    @Transactional
    public AuthenticationResponse register(User testUser) {
        // Создаем пользователя с зашифрованным паролем
        User user = new User();
        user.setName(testUser.getName());
        user.setDateOfBirth(testUser.getDateOfBirth());
        user.setPassword(passwordEncoder.encode(testUser.getPassword()));
        
        // Настраиваем двунаправленные связи
        
        // Email связи
        if (testUser.getEmails() != null && !testUser.getEmails().isEmpty()) {
            for (var email : testUser.getEmails()) {
                email.setUser(user);
                user.getEmails().add(email);
            }
        }
        
        // Телефонные связи
        if (testUser.getPhones() != null && !testUser.getPhones().isEmpty()) {
            for (var phone : testUser.getPhones()) {
                phone.setUser(user);
                user.getPhones().add(phone);
            }
        }
        
        // Связь с аккаунтом
        if (testUser.getAccount() != null) {
            Account account = testUser.getAccount();
            account.setUser(user);
            user.setAccount(account);
        }
        
        // Сохраняем пользователя со всеми связями одной операцией
        // Благодаря настройке CascadeType.ALL, все связанные сущности будут сохранены автоматически
        User savedUser = userRepository.save(user);
        
        // Записываем начальный депозит, если есть аккаунт
        if (savedUser.getAccount() != null) {
            interestService.recordInitialDeposit(savedUser.getAccount());
        }
        
        // Генерируем токены
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        
        // Сохраняем токены
        saveUserToken(accessToken, refreshToken, savedUser);
        
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    private void revokeAllToken(User user) {

        List<Token> validTokens = tokenRepository.findAllAccessTokenByUser(user.getId());

        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> {
                t.setLoggedOut(true);
            });
        }

        tokenRepository.saveAll(validTokens);
    }

    private void saveUserToken(String accessToken, String refreshToken, User user) {

        Token token = new Token();

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);

        tokenRepository.save(token);
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (request.getLogin() == null) {
            throw new IllegalArgumentException("Login cannot be null");
        }
        
        boolean isEmail = request.getLogin().contains("@");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getLogin(),
                        request.getPassword()));

        User user = isEmail
                ? userRepository.findByEmailWithTokens(request.getLogin())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by email"))
                : userRepository.findByPhoneWithTokens(request.getLogin())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found by phone"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        updateUserTokens(user, accessToken, refreshToken);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    @CacheEvict(key = "#user.id")
    private void updateUserTokens(User user, String accessToken, String refreshToken) {
        // Если есть активный токен - обновляем его, иначе создаем новый
        user.getTokens().stream()
                .filter(token -> !token.isLoggedOut())
                .findFirst()
                .ifPresentOrElse(
                        token -> {
                            token.setAccessToken(accessToken);
                            token.setRefreshToken(refreshToken);
                            token.setLoggedOut(false);
                            tokenRepository.save(token);
                        },
                        () -> {
                            Token newToken = new Token();
                            newToken.setAccessToken(accessToken);
                            newToken.setRefreshToken(refreshToken);
                            newToken.setUser(user);
                            tokenRepository.save(newToken);
                        });
    }

    @Transactional
    public org.springframework.http.ResponseEntity<AuthenticationResponse> refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String email = jwtService.extractEmail(refreshToken);

            User user = userRepository.findByEmailWithTokens(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!jwtService.isValidRefresh(refreshToken, user)) {
                return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
            }

            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            revokeAllToken(user);
            saveUserToken(newAccessToken, newRefreshToken, user);

            return org.springframework.http.ResponseEntity.ok(new AuthenticationResponse(newAccessToken, newRefreshToken));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
    }
}
