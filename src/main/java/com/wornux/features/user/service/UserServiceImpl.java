package com.wornux.features.user.service;

import com.vaadin.hilla.BrowserCallable;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.wornux.dto.Gender;
import com.wornux.features.user.domain.SystemRole;
import com.wornux.features.user.domain.User;
import com.wornux.features.user.domain.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@BrowserCallable
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    @Override
    @AnonymousAllowed
    public User createUser(String username, String password, @Nullable String email, String firstName, String lastName,
            @Nullable String phoneNumber, @Nullable LocalDate birthDate, Gender gender, @Nullable String nationality,
            String province, String municipality, String sector, String streetAddress, SystemRole systemRole) {

        validateNewUser(username, email);

        var user = User.builder().username(username).password(passwordEncoder.encode(password)).email(email)
                .firstName(firstName).lastName(lastName).phoneNumber(phoneNumber).birthDate(birthDate).gender(gender)
                .nationality(nationality).province(province).municipality(municipality).sector(sector)
                .streetAddress(streetAddress).systemRole(systemRole).active(true).createdAt(LocalDateTime.now(clock))
                .updatedAt(LocalDateTime.now(clock)).build();

        return userRepository.save(user);
    }

    private void validateNewUser(String username, @Nullable String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("Username already exists");
        }

        if (email != null && userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Email already exists");
        }
    }

    @Override
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public User updateUser(Long userId, User updateData) {
        var currentUser = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !user.getUsername().equals(currentUser.getName())) {
            throw new SecurityException("You can only update your own profile");
        }

        validateUserUpdate(user, updateData);

        updateUserFields(user, updateData);
        user.setUpdatedAt(LocalDateTime.now(clock));

        return userRepository.save(user);
    }

    private void validateUserUpdate(User existingUser, User updateData) {
        if (updateData.getEmail() != null && !updateData.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(updateData.getEmail()).ifPresent(u -> {
                throw new ValidationException("Email already exists");
            });
        }
    }

    private void updateUserFields(User user, User updateData) {
        user.setFirstName(updateData.getFirstName());
        user.setLastName(updateData.getLastName());
        user.setEmail(updateData.getEmail());
        user.setPhoneNumber(updateData.getPhoneNumber());
        user.setBirthDate(updateData.getBirthDate());
        user.setNationality(updateData.getNationality());
        user.setProvince(updateData.getProvince());
        user.setMunicipality(updateData.getMunicipality());
        user.setSector(updateData.getSector());
        user.setStreetAddress(updateData.getStreetAddress());

        if (updateData.getSystemRole() != user.getSystemRole() && SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            user.setSystemRole(updateData.getSystemRole());
        }
    }

    @Override
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public Optional<User> getUserById(Long userId) {
        var currentUser = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findById(userId);

        if (user.isPresent()
                && !currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !user.get().getUsername().equals(currentUser.getName())) {
            return Optional.empty();
        }

        return user;
    }

    @Override
    @AnonymousAllowed
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Secured("ROLE_ADMIN")
    public List<User> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).getContent();
    }

    @Override
    @Secured("ROLE_ADMIN")
    public void deactivateUser(Long userId) {
        updateUserStatus(userId, false);
    }

    @Override
    @Secured("ROLE_ADMIN")
    public void activateUser(Long userId) {
        updateUserStatus(userId, true);
    }

    private void updateUserStatus(Long userId, boolean active) {
        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setActive(active);
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
    }

    @Override
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        var currentUser = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !user.getUsername().equals(currentUser.getName())) {
            throw new SecurityException("You can only change your own password");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
    }

    @Override
    @Secured({ "ROLE_ADMIN", "ROLE_USER" })
    public void updateProfilePicture(Long userId, @Nullable String profilePictureUrl) {
        var currentUser = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
                && !user.getUsername().equals(currentUser.getName())) {
            throw new SecurityException("You can only update your own profile picture");
        }

        user.setProfilePictureUrl(profilePictureUrl);
        user.setUpdatedAt(LocalDateTime.now(clock));
        userRepository.save(user);
    }

    @Override
    @Secured("ROLE_ADMIN")
    public List<User> searchUsers(@Nullable String searchTerm, @Nullable SystemRole systemRole,
            @Nullable Boolean active, Pageable pageable) {

        Specification<User> spec = Specification.where(null);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("email")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("firstName")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("lastName")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("province")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("municipality")), "%" + searchTerm.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("sector")), "%" + searchTerm.toLowerCase() + "%")));
        }

        if (systemRole != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("systemRole"), systemRole));
        }

        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }

        return userRepository.findAll(spec, pageable).getContent();
    }
}