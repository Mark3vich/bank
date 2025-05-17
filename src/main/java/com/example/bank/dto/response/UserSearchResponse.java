package com.example.bank.dto.response;

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
@Schema(description = "Результат поиска пользователей с пагинацией")
public class UserSearchResponse {

    @Schema(description = "Список найденных пользователей")
    private List<UserDto> users;

    @Schema(description = "Общее количество пользователей, удовлетворяющих условиям поиска")
    private long totalElements;

    @Schema(description = "Общее количество страниц")
    private int totalPages;

    @Schema(description = "Текущая страница")
    private int currentPage;

    @Schema(description = "Размер страницы")
    private int pageSize;
}