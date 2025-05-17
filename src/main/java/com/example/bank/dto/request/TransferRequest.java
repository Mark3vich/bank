package com.example.bank.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Schema(description = "DTO для перевода средств от одного пользователя к другому")
public class TransferRequest {
    @Schema(description = "Id пользователя, которму нужно перевести деньги", example = "1")
    @NotNull(message = "Recipient ID is required")
    Long recipientId;

    @Schema(description = "Сумма перевода", example = "100.0")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount;
}
