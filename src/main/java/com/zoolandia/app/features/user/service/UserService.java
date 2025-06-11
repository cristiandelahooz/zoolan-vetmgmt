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

/**
 * Service interface for managing users.
 */
public interface UserService {


    /**
     * Creates a new user.
     *
     * @param username the username
     * @param password the password
     * @param email the email address
     * @param firstName the first name
     * @param lastName the last name
     * @param phoneNumber the phone number
     * @param birthDate the birth date
     * @param gender the gender
     * @param nationality the nationality
     * @param province the province
     * @param municipality the municipality
     * @param sector the sector
     * @param streetAddress the street address
     * @param systemRole the system role
     * @return the created user
     */
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

    /**
     * Updates an existing user.
     *
     * @param userId the user ID
     * @param updateData the user data to update
     * @return the updated user
     */
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    User updateUser(Long userId, User updateData);

    /**
     * Retrieves a user by ID.
     *
     * @param userId the user ID
     * @return an Optional containing the user if found, or empty otherwise
     */
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    Optional<User> getUserById(Long userId);

    /**
     * Retrieves a user by username.
     *
     * @param username the username
     * @return an Optional containing the user if found, or empty otherwise
     */
    Optional<User> getUserByUsername(String username);

    /**
     * Lists users with pagination.
     *
     * @param pageable the pagination information
     * @return a list of users
     */
    @Secured("ROLE_ADMIN")
    List<User> listUsers(Pageable pageable);

    /**
     * Deactivates a user.
     *
     * @param userId the user ID
     */
    @Secured("ROLE_ADMIN")
    void deactivateUser(Long userId);

    /**
     * Activates a user.
     *
     * @param userId the user ID
     */
    @Secured("ROLE_ADMIN")
    void activateUser(Long userId);

    /**
     * Changes the password for a user.
     *
     * @param userId the user ID
     * @param currentPassword the current password
     * @param newPassword the new password
     */
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    void changePassword(Long userId, String currentPassword, String newPassword);

    /**
     * Updates the profile picture URL for a user.
     *
     * @param userId the user ID
     * @param profilePictureUrl the new profile picture URL
     */
    @Secured({"ROLE_ADMIN", "ROLE_USER"})
    void updateProfilePicture(Long userId, @Nullable String profilePictureUrl);

    /**
     * Searches for users by criteria.
     *
     * @param searchTerm the search term
     * @param systemRole the system role
     * @param active whether the user is active
     * @param pageable the pagination information
     * @return a list of users matching the criteria
     */
    @Secured("ROLE_ADMIN")
    List<User> searchUsers(
            @Nullable String searchTerm,
            @Nullable SystemRole systemRole,
            @Nullable Boolean active,
            Pageable pageable);
}