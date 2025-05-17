package com.example.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.dto.request.AuthenticationRequest;
import com.example.bank.dto.request.RefreshTokenRequest;
import com.example.bank.dto.response.AuthenticationResponse;
import com.example.bank.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "Authentication", description = "Эндпоинты для входа и обновления токена")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(summary = "Аутентификация пользователя", description = "Вход в систему с получением JWT токена", responses = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Обновление токена", description = "Обновляет JWT токены с помощью refresh token", responses = {
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Невалидный refresh token", content = @Content)
    })
    @PostMapping("/refresh_token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        ResponseEntity<AuthenticationResponse> response = authenticationService.refreshToken(refreshTokenRequest);

        return response;
    }
}
