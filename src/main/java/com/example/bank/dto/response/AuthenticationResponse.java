package com.example.bank.dto.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Schema(description = "DTO для передачи токенов аутентификации (access token и refresh token)")
public class AuthenticationResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "Токен доступа (access token)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.3Hgk5W_2UtKsI7dpuFjaYmAiRfcsh5y2IYrQINzq4_w", required = true)
    private final String accessToken;

    @Schema(description = "Токен обновления (refresh token)", example = "d2e8d79e-80bb-4b85-bf7b-d7091b999202", required = true)
    private final String refreshToken;

    @JsonCreator
    public AuthenticationResponse(
            @JsonProperty("accessToken") String accessToken, 
            @JsonProperty("refreshToken") String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
