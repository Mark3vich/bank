package com.example.bank.dto.request;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для добавления телефона")
public class PhoneRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "User's phone number (must start with 7 and contain 11 digits)", example = "79123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
    @Pattern(regexp = "^7\\d{10}$", message = "Phone must start with 7 and contain 11 digits")
    private String phone;
}