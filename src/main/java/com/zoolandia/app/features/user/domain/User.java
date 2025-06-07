package com.zoolandia.app.features.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
@Data
@NoArgsConstructor
@SuperBuilder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    protected Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", unique = true)
    protected String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Column(name = "password")
    protected String password;

    @Email(message = "Please provide a valid email address")
    @Column(name = "email", unique = true)
    @Nullable
    protected String email;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name")
    protected String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name")
    protected String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    @Column(name = "phone_number")
    @Nullable
    protected String phoneNumber;

    @Column(name = "birth_date")
    @Nullable
    protected LocalDate birthDate;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    protected Gender gender;

    @Column(name = "nationality")
    @Nullable
    protected String nationality;

    @Column(name = "province")
    @NotNull(message = "La provincia es requerida")
    private String province;

    @Column(name = "municipality")
    @NotNull(message = "El municipio es requerido")
    private String municipality;

    @Column(name = "sector")
    @NotNull(message = "El sector es requerido")
    private String sector;

    @Column(name = "street_address")
    @NotNull(message = "La dirección es requerida")
    private String streetAddress;

    @Column(name = "profile_picture_url")
    @Nullable
    protected String profilePictureUrl;

    @Column(name = "active")
    @Builder.Default
    protected boolean active = true;

    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role")
    protected SystemRole systemRole;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}