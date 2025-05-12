package com.example.bank.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bank.dto.request.EmailRequest;
import com.example.bank.dto.request.PhoneRequest;
import com.example.bank.dto.response.EmailInfoResponse;
import com.example.bank.dto.response.PhoneInfoResponse;
import com.example.bank.dto.response.UserInfoResponse;
import com.example.bank.mapper.EmailMapper;
import com.example.bank.mapper.PhoneMapper;
import com.example.bank.mapper.UserMapper;
import com.example.bank.model.EmailData;
import com.example.bank.model.PhoneData;
import com.example.bank.model.User;
import com.example.bank.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/user")
@Tag(name = "User", description = "Эндпоинты для управления данными пользователя")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;
    private final EmailMapper emailMapper;
    private final PhoneMapper phoneMapper;

    @Autowired
    public UserController(
            UserService userService,
            UserMapper userMapper,
            EmailMapper emailMapper,
            PhoneMapper phoneMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.emailMapper = emailMapper;
        this.phoneMapper = phoneMapper;
    }

    @Operation(summary = "Получить информацию о текущем пользователе", 
            description = "Возвращает данные аутентифицированного пользователя", 
            responses = {
                @ApiResponse(responseCode = "200", description = "Информация о пользователе получена", 
                        content = @Content(schema = @Schema(implementation = UserInfoResponse.class))),
                @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content)
            })
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUser(HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        return ResponseEntity.ok(userMapper.fromUser(user));
    }

    @Operation(summary = "Добавить новый email", description = "Добавляет новый email для текущего пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Email успешно добавлен", content = @Content(schema = @Schema(implementation = EmailInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Email уже занят или неверный формат", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content)
    })
    @PostMapping("/emails")
    public ResponseEntity<EmailInfoResponse> addEmail(@Valid @RequestBody EmailRequest emailRequest,
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        EmailData emailData = userService.addEmail(user, emailRequest);
        return ResponseEntity.ok(emailMapper.fromEmailData(emailData));
    }

    @Operation(summary = "Обновить email", description = "Обновляет указанный email пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Email успешно обновлен", content = @Content),
            @ApiResponse(responseCode = "400", description = "Email уже занят или неверный формат", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Email не найден", content = @Content)
    })
    @PutMapping("/emails/{emailId}")
    public ResponseEntity<Void> updateEmail(
            @PathVariable Long emailId,
            @Valid @RequestBody EmailRequest emailRequest, 
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        userService.updateEmail(user, emailId, emailRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить email", description = "Удаляет указанный email пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Email успешно удален", content = @Content),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить единственный email", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Email не найден", content = @Content)
    })
    @DeleteMapping("/emails/{emailId}")
    public ResponseEntity<Void> deleteEmail(@PathVariable Long emailId, 
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        userService.deleteEmail(user, emailId);
        return ResponseEntity.ok().build();
    }

    // Phone operations
    @Operation(summary = "Добавить новый телефон", description = "Добавляет новый телефон для текущего пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Телефон успешно добавлен", content = @Content(schema = @Schema(implementation = PhoneInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Телефон уже занят или неверный формат", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content)
    })
    @PostMapping("/phones")
    public ResponseEntity<PhoneInfoResponse> addPhone(@Valid @RequestBody PhoneRequest phoneRequest,
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        PhoneData phoneData = userService.addPhone(user, phoneRequest);
        return ResponseEntity.ok(phoneMapper.fromPhoneData(phoneData));
    }

    @Operation(summary = "Обновить телефон", description = "Обновляет указанный телефон пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Телефон успешно обновлен", content = @Content),
            @ApiResponse(responseCode = "400", description = "Телефон уже занят или неверный формат", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Телефон не найден", content = @Content)
    })
    @PutMapping("/phones/{phoneId}")
    public ResponseEntity<Void> updatePhone(
            @PathVariable Long phoneId,
            @Valid @RequestBody PhoneRequest phoneRequest, 
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        userService.updatePhone(user, phoneId, phoneRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить телефон", description = "Удаляет указанный телефон пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Телефон успешно удален", content = @Content),
            @ApiResponse(responseCode = "400", description = "Нельзя удалить единственный телефон", content = @Content),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован", content = @Content),
            @ApiResponse(responseCode = "404", description = "Телефон не найден", content = @Content)
    })
    @DeleteMapping("/phones/{phoneId}")
    public ResponseEntity<Void> deletePhone(@PathVariable Long phoneId, 
            HttpServletRequest request) {
        User user = userService.getUserFromRequest(request);
        userService.deletePhone(user, phoneId);
        return ResponseEntity.ok().build();
    }
}
