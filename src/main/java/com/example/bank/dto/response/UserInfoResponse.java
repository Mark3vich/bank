package com.example.bank.dto.response;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Объект ответа, содержащий информацию о пользователе")
public class UserInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Уникальный идентификатор пользователя", example = "123")
    private Long id;

    @Schema(description = "Полное имя пользователя", example = "John Doe")
    private String name;

    @Schema(description = "Дата рождения в формате ГГГГ-ММ-ДД", example = "1990-01-15")
    private String dateOfBirth;

    @Schema(description = "Список адресов электронной почты пользователя")
    private List<EmailInfoResponse> emails;

    @Schema(description = "Список телефонных номеров пользователя")
    private List<PhoneInfoResponse> phones;
}