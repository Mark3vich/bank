package com.example.bank.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.bank.dto.request.AuthenticationRequest;
import com.example.bank.dto.request.RefreshTokenRequest;
import com.example.bank.dto.response.AuthenticationResponse;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.Token;
import com.example.bank.model.User;
import com.example.bank.repository.TokenRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.service.AuthenticationService;
import com.example.bank.service.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthenticationUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private List<Token> testTokens;
    private String testAccessToken;
    private String testRefreshToken;

    @BeforeEach
    public void setUp() {
        // Подготовка тестового пользователя
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Тестовый Пользователь");
        testUser.setPassword("encodedPassword123");
        testUser.setDateOfBirth("01.01.1990");

        // Добавляем email
        EmailData email = new EmailData();
        email.setEmail("test@example.com");
        email.setUser(testUser);
        testUser.getEmails().add(email);

        // Добавляем телефон
        PhoneData phone = new PhoneData();
        phone.setPhone("79001234567");
        phone.setUser(testUser);
        testUser.getPhones().add(phone);

        // Подготовка тестовых токенов
        testAccessToken = "test.access.token";
        testRefreshToken = "test.refresh.token";

        Token token = new Token();
        token.setId(1L);
        token.setAccessToken(testAccessToken);
        token.setRefreshToken(testRefreshToken);
        token.setLoggedOut(false);
        token.setUser(testUser);

        testTokens = new ArrayList<>();
        testTokens.add(token);
    }

    @Test
    public void testAuthenticate_WithEmail_ShouldReturnTokens() {
        // Подготовка
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLogin("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmailWithTokens(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(testAccessToken);
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn(testRefreshToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(testTokens.get(0));

        // Выполнение
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Проверка
        assertNotNull(response);
        assertEquals(testAccessToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        verify(userRepository).findByEmailWithTokens(request.getLogin());
    }

    @Test
    public void testAuthenticate_WithPhone_ShouldReturnTokens() {
        // Подготовка
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLogin("79001234567");
        request.setPassword("password123");

        when(userRepository.findByPhoneWithTokens(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn(testAccessToken);
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn(testRefreshToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(testTokens.get(0));

        // Выполнение
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Проверка
        assertNotNull(response);
        assertEquals(testAccessToken, response.getAccessToken());
        assertEquals(testRefreshToken, response.getRefreshToken());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        verify(userRepository).findByPhoneWithTokens(request.getLogin());
    }

    @Test
    public void testAuthenticate_UserNotFound_ShouldThrowException() {
        // Подготовка
        AuthenticationRequest request = new AuthenticationRequest();
        request.setLogin("nonexistent@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmailWithTokens(anyString())).thenReturn(Optional.empty());

        // Выполнение и проверка
        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticate(request);
        });
    }

    @Test
    public void testRefreshToken_ValidToken_ShouldReturnNewTokens() {
        // Подготовка
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(testRefreshToken);

        when(jwtService.extractEmail(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmailWithTokens(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.isValidRefresh(anyString(), any(User.class))).thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("new.access.token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("new.refresh.token");
        when(tokenRepository.findAllAccessTokenByUser(any(Long.class))).thenReturn(testTokens);
        when(tokenRepository.saveAll(any())).thenReturn(testTokens);
        when(tokenRepository.save(any(Token.class))).thenReturn(testTokens.get(0));

        // Выполнение
        ResponseEntity<AuthenticationResponse> response = authenticationService.refreshToken(refreshRequest);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new.access.token", response.getBody().getAccessToken());
        assertEquals("new.refresh.token", response.getBody().getRefreshToken());
        verify(tokenRepository).findAllAccessTokenByUser(testUser.getId());
        verify(tokenRepository).saveAll(any());
        verify(jwtService).isValidRefresh(testRefreshToken, testUser);
    }

    @Test
    public void testRefreshToken_InvalidToken_ShouldReturnUnauthorized() {
        // Подготовка
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(testRefreshToken);

        when(jwtService.extractEmail(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmailWithTokens(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.isValidRefresh(anyString(), any(User.class))).thenReturn(false);

        // Выполнение
        ResponseEntity<AuthenticationResponse> response = authenticationService.refreshToken(refreshRequest);

        // Проверка
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testRefreshToken_EmptyToken_ShouldReturnUnauthorized() {
        // Подготовка
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken("");

        // Выполнение
        ResponseEntity<AuthenticationResponse> response = authenticationService.refreshToken(refreshRequest);

        // Проверка
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testRefreshToken_UserNotFound_ShouldThrowException() {
        // Подготовка
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(testRefreshToken);

        when(jwtService.extractEmail(anyString())).thenReturn("test@example.com");
        when(userRepository.findByEmailWithTokens(anyString())).thenReturn(Optional.empty());

        // Выполнение и проверка
        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.refreshToken(refreshRequest);
        });
    }
}
