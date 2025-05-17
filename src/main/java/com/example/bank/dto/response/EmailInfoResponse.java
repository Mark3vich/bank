package com.example.bank.dto.response;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект ответа, содержащий информацию об электронной почте")
public class EmailInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Уникальный идентификатор записи электронной почты", example = "1")
    private Long id;

    @Schema(description = "Email адрес", example = "user@example.com")
    private String email;
}