package com.example.bank.dto.response;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import com.example.bank.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private String dateOfBirth;
    private List<EmailInfoResponse> emails;
    private List<PhoneInfoResponse> phones;
    
    public static UserInfoResponse fromUser(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dateOfBirth(user.getDateOfBirth())
                .emails(user.getEmails().stream()
                        .map(EmailInfoResponse::fromEmailData)
                        .collect(Collectors.toList()))
                .phones(user.getPhones().stream()
                        .map(PhoneInfoResponse::fromPhoneData)
                        .collect(Collectors.toList()))
                .build();
    }
} 