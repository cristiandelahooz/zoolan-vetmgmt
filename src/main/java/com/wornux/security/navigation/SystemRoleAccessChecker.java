package com.wornux.security.navigation;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
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
public class SystemRoleAccessChecker implements NavigationAccessChecker {

    private final SecurityContextService securityContextService;

    @Override
    public AccessCheckResult check(NavigationContext context) {
        if (context.isErrorHandling()) {
            return AccessCheckResult.neutral();
        }

        Class<?> targetClass = context.getNavigationTarget();
        
        RequiredSystemRoles annotation = targetClass.getAnnotation(RequiredSystemRoles.class);
        if (annotation == null) {
            return AccessCheckResult.neutral();
        }

        if (!securityContextService.isAuthenticated()) {
            log.debug("Access denied to {} - user not authenticated", targetClass.getSimpleName());
            return AccessCheckResult.deny("Authentication required");
        }

        Optional<SystemRole> currentRole = securityContextService.getCurrentSystemRole();
        if (currentRole.isEmpty()) {
            log.warn("User {} has no system role assigned", securityContextService.getCurrentUsername());
            return AccessCheckResult.deny("No role assigned");
        }

        SystemRole[] requiredRoles = annotation.value();
        boolean hasRequiredRole = Arrays.asList(requiredRoles).contains(currentRole.get());

        if (hasRequiredRole) {
            log.debug("Access granted to {} for user {} with role {}", 
                targetClass.getSimpleName(), 
                securityContextService.getCurrentUsername(),
                currentRole.get());
            return AccessCheckResult.allow();
        } else {
            log.debug("Access denied to {} for user {} - required roles: {}, current role: {}", 
                targetClass.getSimpleName(),
                securityContextService.getCurrentUsername(),
                Arrays.toString(requiredRoles),
                currentRole.get());
            return AccessCheckResult.deny("Insufficient privileges");
        }
    }
}