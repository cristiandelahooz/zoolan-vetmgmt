package com.wornux.security.service;

import com.wornux.data.entity.Employee;
import com.wornux.data.entity.User;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.UserDetailsImpl;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for accessing security context and checking user permissions. Provides centralized access
 * to current user information and role validation.
 */
@Slf4j
@Service
public class SecurityContextService {

  /**
   * Get the currently authenticated user.
   *
   * @return Optional containing the current user, or empty if not authenticated
   */
  public Optional<User> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      log.debug("No authenticated user found");
      return Optional.empty();
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetailsImpl userDetails) {
      return Optional.of(userDetails.user());
    }

    log.warn("Principal is not UserDetailsImpl: {}", principal.getClass().getName());
    return Optional.empty();
  }

  /**
   * Get the current user as an Employee if they are one.
   *
   * @return Optional containing the current employee, or empty if not an employee
   */
  public Optional<Employee> getCurrentEmployee() {
    return getCurrentUser().filter(user -> user instanceof Employee).map(user -> (Employee) user);
  }

  /**
   * Get the username of the currently authenticated user.
   *
   * @return Username or "anonymous" if not authenticated
   */
  public String getCurrentUsername() {
    return getCurrentUser().map(User::getUsername).orElse("anonymous");
  }

  /**
   * Get the SystemRole of the current user.
   *
   * @return Optional containing the system role
   */
  public Optional<SystemRole> getCurrentSystemRole() {
    return getCurrentUser().map(User::getSystemRole);
  }

  /**
   * Get the EmployeeRole of the current user if they are an employee.
   *
   * @return Optional containing the employee role
   */
  public Optional<EmployeeRole> getCurrentEmployeeRole() {
    return getCurrentEmployee().map(Employee::getEmployeeRole);
  }

  /**
   * Check if the current user has a specific system role.
   *
   * @param role The system role to check
   * @return true if the user has the role
   */
  public boolean hasSystemRole(SystemRole role) {
    if (role == null) {
      log.debug("hasSystemRole called with null role");
      return false;
    }

    Optional<SystemRole> currentRole = getCurrentSystemRole();
    boolean hasRole = currentRole.map(userRole -> userRole == role).orElse(false);

    log.info(
        "hasSystemRole check - Required: {}, Current: {}, Result: {}, User: {}",
        role,
        currentRole.orElse(null),
        hasRole,
        getCurrentUsername());

    return hasRole;
  }

  /**
   * Check if the current user has any of the specified system roles.
   *
   * @param roles The system roles to check
   * @return true if the user has any of the roles
   */
  public boolean hasAnySystemRole(SystemRole... roles) {
    if (roles == null || roles.length == 0) {
      return false;
    }

    Optional<SystemRole> currentRole = getCurrentSystemRole();
    if (currentRole.isEmpty()) {
      return false;
    }

    for (SystemRole role : roles) {
      if (role == currentRole.get()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the current user has all of the specified system roles. Since a user can only have one
   * system role, this returns true only if exactly one role is specified.
   *
   * @param roles The system roles to check
   * @return true if the user has all roles (only possible with single role)
   */
  public boolean hasAllSystemRoles(SystemRole... roles) {
    if (roles == null || roles.length != 1) {
      return false;
    }
    return hasSystemRole(roles[0]);
  }

  /**
   * Check if the current user has a specific employee role.
   *
   * @param role The employee role to check
   * @return true if the user has the role
   */
  public boolean hasEmployeeRole(EmployeeRole role) {
    if (role == null) {
      return false;
    }
    return getCurrentEmployeeRole().map(userRole -> userRole == role).orElse(false);
  }

  /**
   * Check if the current user has any of the specified employee roles.
   *
   * @param roles The employee roles to check
   * @return true if the user has any of the roles
   */
  public boolean hasAnyEmployeeRole(EmployeeRole... roles) {
    if (roles == null || roles.length == 0) {
      return false;
    }

    Optional<EmployeeRole> currentRole = getCurrentEmployeeRole();
    if (currentRole.isEmpty()) {
      return false;
    }

    for (EmployeeRole role : roles) {
      if (role == currentRole.get()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the current user is authenticated.
   *
   * @return true if authenticated
   */
  public boolean isAuthenticated() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal());
  }

  /**
   * Get all authorities (roles) of the current user.
   *
   * @return Set of authority strings
   */
  public Set<String> getCurrentAuthorities() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return Set.of();
    }

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
  }

  /**
   * Check if the current user has a specific Spring Security authority.
   *
   * @param authority The authority to check (e.g., "ROLE_ADMIN")
   * @return true if the user has the authority
   */
  public boolean hasAuthority(String authority) {
    return getCurrentAuthorities().contains(authority);
  }

  /** Log current security context for debugging. */
  public void logSecurityContext() {
    log.debug("=== Security Context Debug ===");
    log.debug("Authenticated: {}", isAuthenticated());
    log.debug("Username: {}", getCurrentUsername());
    log.debug("System Role: {}", getCurrentSystemRole().orElse(null));
    log.debug("Employee Role: {}", getCurrentEmployeeRole().orElse(null));
    log.debug("Authorities: {}", getCurrentAuthorities());
    log.debug("==============================");
  }
}

