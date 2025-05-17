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
@Schema(description = "Объект ответа, содержащий информацию о телефоне")
public class PhoneInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "Уникальный идентификатор телефонной записи", example = "1")
    private Long id;

    @Schema(description = "Необработанный телефонный номер (11 цифр, начинающихся с 7)", example = "79123456789")
    private String phone;

    @Schema(description = "Отформатированный номер телефона", example = "+7 (912) 345-67-89")
    private String formattedPhone;
}