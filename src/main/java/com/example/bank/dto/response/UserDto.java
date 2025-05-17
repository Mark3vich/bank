package com.example.bank.dto.response;

import java.math.BigDecimal;
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
@Schema(description = "Данные пользователя")
public class UserDto {

    @Schema(description = "Идентификатор пользователя")
    private Long id;

    @Schema(description = "Имя пользователя")
    private String name;

    @Schema(description = "Дата рождения")
    private String dateOfBirth;

    @Schema(description = "Email адреса пользователя")
    private List<String> emails;

    @Schema(description = "Телефоны пользователя")
    private List<String> phones;

    @Schema(description = "Баланс счета")
    private BigDecimal balance;
}