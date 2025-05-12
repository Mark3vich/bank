package com.example.bank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description="DTO для обновления токена")
public class RefreshTokenRequest {
    @Schema(description = "Refresh-токен для обновления access-токена", example = "your_refresh_token_here")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
