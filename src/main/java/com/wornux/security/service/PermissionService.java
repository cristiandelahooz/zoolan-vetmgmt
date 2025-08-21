package com.wornux.security.service;

import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.annotations.RequiredEmployeeRoles;
import com.wornux.security.annotations.RequiredSystemRoles;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for checking permissions based on annotations. Evaluates role-based access control for views and components.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    @Getter
    private final SecurityContextService securityContextService;

    /**
     * Check if the current user has permission to access a class based on its annotations.
     *
     * @param targetClass
     *         The class to check permissions for
     * @return true if access is allowed, false otherwise
     */
    public boolean hasPermission(Class<?> targetClass) {
        if (targetClass == null) {
            log.warn("Cannot check permissions for null class");
            return false;
        }

        // SYSTEM_ADMIN has access to everything
        if (securityContextService.hasSystemRole(SystemRole.SYSTEM_ADMIN)) {
            log.info("Access granted to {} - SYSTEM_ADMIN bypass (User: {}, Role: {})", 
                targetClass.getSimpleName(),
                securityContextService.getCurrentUsername(),
                securityContextService.getCurrentSystemRole().orElse(null));
            return true;
        }

        // Check system role requirements
        RequiredSystemRoles systemRoles = targetClass.getAnnotation(RequiredSystemRoles.class);
        boolean systemRolesMet = (systemRoles == null) || checkSystemRoles(systemRoles);

        // Check employee role requirements
        RequiredEmployeeRoles employeeRoles = targetClass.getAnnotation(RequiredEmployeeRoles.class);
        boolean employeeRolesMet = (employeeRoles == null) || checkEmployeeRoles(employeeRoles);

        // If both annotations are present, user needs to meet at least one requirement (OR logic)
        // If only one annotation is present, user needs to meet that requirement
        if (systemRoles != null && employeeRoles != null) {
            if (!systemRolesMet && !employeeRolesMet) {
                log.debug("Access denied to {} - neither system role nor employee role requirements met", 
                    targetClass.getSimpleName());
                return false;
            }
        } else if (systemRoles != null && !systemRolesMet) {
            log.debug("Access denied to {} - system role requirement not met", targetClass.getSimpleName());
            return false;
        } else if (employeeRoles != null && !employeeRolesMet) {
            log.debug("Access denied to {} - employee role requirement not met", targetClass.getSimpleName());
            return false;
        }

        // If no annotations present, check if user is at least authenticated
        if (systemRoles == null && employeeRoles == null) {
            boolean authenticated = securityContextService.isAuthenticated();
            if (!authenticated) {
                log.debug("Access denied to {} - user not authenticated", targetClass.getSimpleName());
            }
            return authenticated;
        }

        log.debug("Access granted to {}", targetClass.getSimpleName());
        return true;
    }

    /**
     * Check if system role requirements are met.
     *
     * @param annotation
     *         The RequiredSystemRoles annotation
     * @return true if requirements are met
     */
    private boolean checkSystemRoles(RequiredSystemRoles annotation) {
        SystemRole[] requiredRoles = annotation.value();

        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // No specific roles required
        }

        if (annotation.requireAll()) {
            // User must have all specified roles (practically impossible with single role per user)
            return securityContextService.hasAllSystemRoles(requiredRoles);
        } else {
            // User must have at least one of the specified roles
            return securityContextService.hasAnySystemRole(requiredRoles);
        }
    }

    /**
     * Check if employee role requirements are met.
     *
     * @param annotation
     *         The RequiredEmployeeRoles annotation
     * @return true if requirements are met
     */
    private boolean checkEmployeeRoles(RequiredEmployeeRoles annotation) {
        EmployeeRole[] requiredRoles = annotation.value();

        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // No specific roles required
        }

        // If user is not an employee, they can't have employee roles
        if (securityContextService.getCurrentEmployee().isEmpty()) {
            return false;
        }

        if (annotation.requireAll()) {
            // User must have all specified roles (practically impossible with single role per user)
            // This would only return true if exactly one role is specified
            return requiredRoles.length == 1 && securityContextService.hasEmployeeRole(requiredRoles[0]);
        } else {
            // User must have at least one of the specified roles
            return securityContextService.hasAnyEmployeeRole(requiredRoles);
        }
    }

    /**
     * Get the access denied message for a class.
     *
     * @param targetClass
     *         The class to get the message for
     * @return Custom access denied message or default message
     */
    public String getAccessDeniedMessage(Class<?> targetClass) {
        if (targetClass == null) {
            return "Acceso denegado";
        }

        // Check for custom message in system roles annotation
        RequiredSystemRoles systemRoles = targetClass.getAnnotation(RequiredSystemRoles.class);
        if (systemRoles != null && !systemRoles.accessDeniedMessage().isEmpty()) {
            return systemRoles.accessDeniedMessage();
        }

        // Check for custom message in employee roles annotation
        RequiredEmployeeRoles employeeRoles = targetClass.getAnnotation(RequiredEmployeeRoles.class);
        if (employeeRoles != null && !employeeRoles.accessDeniedMessage().isEmpty()) {
            return employeeRoles.accessDeniedMessage();
        }

        // Generate default message based on requirements
        StringBuilder message = new StringBuilder("No tiene permisos para acceder a esta sección. ");

        if (systemRoles != null) {
            message.append("Roles de sistema requeridos: ");
            for (int i = 0; i < systemRoles.value().length; i++) {
                if (i > 0)
                    message.append(", ");
                message.append(systemRoles.value()[i].getDisplayName());
            }
            message.append(". ");
        }

        if (employeeRoles != null) {
            message.append("Roles de empleado requeridos: ");
            for (int i = 0; i < employeeRoles.value().length; i++) {
                if (i > 0)
                    message.append(", ");
                message.append(employeeRoles.value()[i].getDisplayName());
            }
            message.append(".");
        }

        return message.toString();
    }

    /**
     * Log permission check details for debugging.
     *
     * @param targetClass
     *         The class being checked
     */
    public void logPermissionCheck(Class<?> targetClass) {
        log.debug("=== Permission Check for {} ===", targetClass.getSimpleName());

        RequiredSystemRoles systemRoles = targetClass.getAnnotation(RequiredSystemRoles.class);
        if (systemRoles != null) {
            log.debug("Required System Roles: {}", (Object) systemRoles.value());
            log.debug("Require All: {}", systemRoles.requireAll());
        }

        RequiredEmployeeRoles employeeRoles = targetClass.getAnnotation(RequiredEmployeeRoles.class);
        if (employeeRoles != null) {
            log.debug("Required Employee Roles: {}", (Object) employeeRoles.value());
            log.debug("Require All: {}", employeeRoles.requireAll());
        }

        securityContextService.logSecurityContext();
        log.debug("Permission Granted: {}", hasPermission(targetClass));
        log.debug("================================");
    }
}