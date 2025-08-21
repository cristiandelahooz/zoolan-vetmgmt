package com.wornux.security.navigation;

import com.wornux.data.enums.SystemRole;
import com.wornux.security.annotations.RequiredSystemRoles;
import com.wornux.security.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemRoleAccessChecker {

    private final SecurityContextService securityContextService;

    public boolean hasAccess(Class<?> targetClass) {
        RequiredSystemRoles annotation = targetClass.getAnnotation(RequiredSystemRoles.class);
        if (annotation == null) {
            return true;
        }

        if (!securityContextService.isAuthenticated()) {
            log.debug("Access denied to {} - user not authenticated", targetClass.getSimpleName());
            return false;
        }

        Optional<SystemRole> currentRole = securityContextService.getCurrentSystemRole();
        if (currentRole.isEmpty()) {
            log.warn("User {} has no system role assigned", securityContextService.getCurrentUsername());
            return false;
        }

        SystemRole[] requiredRoles = annotation.value();
        boolean hasRequiredRole = Arrays.asList(requiredRoles).contains(currentRole.get());

        if (hasRequiredRole) {
            log.debug("Access granted to {} for user {} with role {}", 
                targetClass.getSimpleName(), 
                securityContextService.getCurrentUsername(),
                currentRole.get());
            return true;
        } else {
            log.debug("Access denied to {} for user {} - required roles: {}, current role: {}", 
                targetClass.getSimpleName(),
                securityContextService.getCurrentUsername(),
                Arrays.toString(requiredRoles),
                currentRole.get());
            return false;
        }
    }
}