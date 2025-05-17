package com.example.bank.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "users")
public class User implements UserDetails {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "date_of_birth")
    @Pattern(regexp = "^\\d{2}\\.\\d{2}\\.\\d{4}$", message = "Date format must be dd.MM.yyyy")
    private String dateOfBirth;

    @Column(nullable = false)
    @Size(min = 8, max = 500, message = "Password must be 8-500 characters")
    @NotBlank
    private String password;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private Account account;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmailData> emails = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PhoneData> phones = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Token> tokens = new HashSet<>();

    public void addPhone(PhoneData phone) {
        phones.add(phone);
        phone.setUser(this);
    }

    public void removePhone(PhoneData phone) {
        phones.remove(phone);
        phone.setUser(null);
    }

    public void addEmail(EmailData email) {
        emails.add(email);
        email.setUser(this);
    }
    
    public void removeEmail(EmailData email) {
        emails.remove(email);
        email.setUser(null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return this.name;
    }
}
