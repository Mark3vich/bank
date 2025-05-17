package com.example.bank.dto.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO для логина пользователя, включающее email/телефон и пароль")
public class AuthenticationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "Login cannot be null")
    @NotBlank(message = "Email or phone is required")
    @Schema(description = "Адрес электронной почты или телефон пользователя", example = "user@example.com или 79207865432", required = true)
    private String login;

    @NotNull(message = "Password cannot be null")
    @NotBlank(message = "Password is required")
    @Schema(description = "Пароль пользователя", example = "password123", required = true)
    private String password;

    @JsonCreator
    public AuthenticationRequest(
            @JsonProperty("login") String login,
            @JsonProperty("password") String password) {
        this.login = login;
        this.password = password;
    }
}
