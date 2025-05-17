package com.example.bank.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Параметры поиска пользователей")
public class UserSearchRequest {

    @Schema(description = "Имя пользователя для поиска по like шаблону", example = "Иван")
    private String name;

    @Schema(description = "Email для точного поиска", example = "user@example.com")
    private String email;

    @Schema(description = "Телефон для точного поиска", example = "79001112233")
    private String phone;

    @Schema(description = "Дата рождения для фильтрации пользователей, родившихся после указанной даты", example = "01.01.1990")
    private String dateOfBirth;

    @Schema(description = "Размер страницы для пагинации", example = "10")
    private Integer size = 10;

    @Schema(description = "Номер страницы для пагинации (начиная с 0)", example = "0")
    private Integer page = 0;
}