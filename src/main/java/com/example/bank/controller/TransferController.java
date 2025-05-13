package com.example.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.dto.request.TransferRequest;
import com.example.bank.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/transfer")
@Tag(name = "Transfer", description = "Эндпоинты для перевода денежных средств")
public class TransferController {
    private final TransferService transferService;

    @Operation(summary = "Перевод денег между пользователями", description = "Выполняет перевод указанной суммы от авторизованного пользователя (из токена) к выбранному получателю.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные запроса", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Получатель не найден", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "422", description = "Недостаточно средств для выполнения перевода", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Попытка перевода самому себе", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/money")
    public ResponseEntity<Void> transferMoney(
            @Parameter(description = "Данные перевода: получатель и сумма", required = true, schema = @Schema(implementation = TransferRequest.class)) @Valid @RequestBody TransferRequest transferRequest,

            @Parameter(hidden = true) HttpServletRequest request) {
        transferService.transferMoney(transferRequest, request);
        return ResponseEntity.ok().build();
    }
}
