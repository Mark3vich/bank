package com.example.bank.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
} 