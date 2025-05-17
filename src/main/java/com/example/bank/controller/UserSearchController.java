package com.example.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.dto.request.UserSearchRequest;
import com.example.bank.dto.response.UserSearchResponse;
import com.example.bank.service.UserSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Search", description = "API для поиска пользователей")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @Operation(summary = "Поиск пользователей", description = "Поиск пользователей с фильтрацией по различным полям и пагинацией. "
            +
            "Поиск по имени использует Redis для быстрого поиска префиксов.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный поиск пользователей", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserSearchResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
    })
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUsers(
            @Parameter(description = "Имя пользователя (поиск по началу имени)") @RequestParam(required = false) String name,

            @Parameter(description = "Email для точного поиска") @RequestParam(required = false) String email,

            @Parameter(description = "Телефон для точного поиска") @RequestParam(required = false) String phone,

            @Parameter(description = "Дата рождения (поиск пользователей, родившихся после указанной даты)") @RequestParam(required = false) String dateOfBirth,

            @Parameter(description = "Размер страницы") @RequestParam(required = false, defaultValue = "10") Integer size,

            @Parameter(description = "Номер страницы (начиная с 0)") @RequestParam(required = false, defaultValue = "0") Integer page) {
        UserSearchRequest request = new UserSearchRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPhone(phone);
        request.setDateOfBirth(dateOfBirth);
        request.setSize(size);
        request.setPage(page);

        UserSearchResponse response = userSearchService.searchUsers(request);
        return ResponseEntity.ok(response);
    }
}