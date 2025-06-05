
package com.zoolandia.app.features.user.service;

import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.user.domain.Gender;
import com.zoolandia.app.features.user.domain.User;
import com.zoolandia.app.features.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserService {

	/**
	 * Creates a new user in the system
	 *
	 * @param username        unique username for the user
	 * @param password        user's password
	 * @param email          user's email address
	 * @param firstName      user's first name
	 * @param lastName       user's last name
	 * @param phoneNumber    user's phone number
	 * @param birthDate      user's date of birth
	 * @param gender         user's gender
	 * @param nationality    user's nationality
	 * @param address        user's address
	 * @param role          user's role in the system
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
					@Nullable String address,
					UserRole role
	);

	/**
	 * Updates an existing user's information
	 *
	 * @param userId user ID to update
	 * @param updateData updated user information
	 * @return the updated user
	 */
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	User updateUser(Long userId, User updateData);

	/**
	 * Retrieves a user by their ID
	 *
	 * @param userId the ID of the user to retrieve
	 * @return the user if found
	 */
	Optional<User> getUserById(Long userId);

	/**
	 * Retrieves a user by their username
	 *
	 * @param username the username to search for
	 * @return the user if found
	 */
	Optional<User> getUserByUsername(String username);

	/**
	 * Lists all users with pagination
	 *
	 * @param pageable pagination information
	 * @return list of users
	 */
	@Secured("ROLE_ADMIN")
	List<User> listUsers(Pageable pageable);

	/**
	 * Deactivates a user account
	 *
	 * @param userId the ID of the user to deactivate
	 */
	@Secured("ROLE_ADMIN")
	void deactivateUser(Long userId);

	/**
	 * Activates a user account
	 *
	 * @param userId the ID of the user to activate
	 */
	@Secured("ROLE_ADMIN")
	void activateUser(Long userId);

	/**
	 * Changes a user's password
	 *
	 * @param userId the ID of the user
	 * @param currentPassword the current password
	 * @param newPassword the new password
	 */
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	void changePassword(
					Long userId,
					@NotBlank String currentPassword,
					@NotBlank @Size(min = 8) String newPassword
	);

	/**
	 * Updates a user's profile picture
	 *
	 * @param userId the ID of the user
	 * @param profilePictureUrl the URL of the new profile picture
	 */
	@Secured({"ROLE_ADMIN", "ROLE_USER"})
	void updateProfilePicture(Long userId, @Nullable String profilePictureUrl);

	/**
	 * Searches for users based on various criteria
	 *
	 * @param searchTerm the search term to match against name, email, or username
	 * @param role optional role filter
	 * @param active optional active status filter
	 * @param pageable pagination information
	 * @return list of matching users
	 */
	@Secured("ROLE_ADMIN")
	List<User> searchUsers(
					@Nullable String searchTerm,
					@Nullable UserRole role,
					@Nullable Boolean active,
					Pageable pageable
	);
}