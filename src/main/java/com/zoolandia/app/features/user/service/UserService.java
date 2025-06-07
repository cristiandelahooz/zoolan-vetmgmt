package com.zoolandia.app.features.user.service;

import com.zoolandia.app.features.user.domain.Gender;
import com.zoolandia.app.features.user.domain.SystemRole;
import com.zoolandia.app.features.user.domain.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserService {

    @Secured("ROLE_ADMIN")
    User createUser(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 8) String password,
            @Nullable @Email String email,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @Nullable String phoneNumber,
            @Nullable LocalDate birthDate,
            Gender gender,
            @Nullable String nationality,
            @NotNull String province,
            @NotNull String municipality,
            @NotNull String sector,
            @NotNull String streetAddress,
            SystemRole systemRole
    );

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    User updateUser(Long userId, User updateData);

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    Optional<User> getUserById(Long userId);

    Optional<User> getUserByUsername(String username);

    @Secured("ROLE_ADMIN")
    List<User> listUsers(Pageable pageable);

    @Secured("ROLE_ADMIN")
    void deactivateUser(Long userId);

    @Secured("ROLE_ADMIN")
    void activateUser(Long userId);

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    void changePassword(Long userId, String currentPassword, String newPassword);

    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    void updateProfilePicture(Long userId, @Nullable String profilePictureUrl);

    @Secured("ROLE_ADMIN")
    List<User> searchUsers(
            @Nullable String searchTerm,
            @Nullable SystemRole systemRole,
            @Nullable Boolean active,
            Pageable pageable);
}