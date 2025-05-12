package com.example.bank.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phone_data")
public class PhoneData implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone", length = 13, nullable = false, unique = true)
    @Size(min = 11, max = 11, message = "Phone number must be 11 digits")
    @Pattern(regexp = "^7\\d{10}$", message = "Phone must start with 7 and contain 11 digits")
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public String getFormattedPhone() {
        return "+" + phone.substring(0,1) + " (" + phone.substring(1,4) + ") " + 
               phone.substring(4,7) + "-" + phone.substring(7,9) + "-" + phone.substring(9);
    }
}
