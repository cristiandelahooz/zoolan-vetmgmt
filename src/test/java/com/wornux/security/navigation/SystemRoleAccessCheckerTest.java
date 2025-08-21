package com.wornux.security.navigation;

import com.wornux.data.enums.SystemRole;
import com.wornux.security.annotations.RequiredSystemRoles;
import com.wornux.security.service.SecurityContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemRoleAccessCheckerTest {

    @Mock
    private SecurityContextService securityContextService;

    @InjectMocks
    private SystemRoleAccessChecker systemRoleAccessChecker;

    @Test
    void hasAccess_ShouldReturnTrue_WhenNoAnnotationPresent() {
        boolean result = systemRoleAccessChecker.hasAccess(ViewWithoutAnnotation.class);

        assertTrue(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenUserNotAuthenticated() {
        when(securityContextService.isAuthenticated()).thenReturn(false);

        boolean result = systemRoleAccessChecker.hasAccess(AdminOnlyView.class);

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenUserHasNoRole() {
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.empty());
        when(securityContextService.getCurrentUsername()).thenReturn("testuser");

        boolean result = systemRoleAccessChecker.hasAccess(AdminOnlyView.class);

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenUserHasRequiredRole() {
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.SYSTEM_ADMIN));
        when(securityContextService.getCurrentUsername()).thenReturn("admin");

        boolean result = systemRoleAccessChecker.hasAccess(AdminOnlyView.class);

        assertTrue(result);
    }

    @Test
    void hasAccess_ShouldReturnFalse_WhenUserLacksRequiredRole() {
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.USER));
        when(securityContextService.getCurrentUsername()).thenReturn("user");

        boolean result = systemRoleAccessChecker.hasAccess(AdminOnlyView.class);

        assertFalse(result);
    }

    @Test
    void hasAccess_ShouldReturnTrue_WhenUserHasOneOfMultipleRequiredRoles() {
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.MANAGER));
        when(securityContextService.getCurrentUsername()).thenReturn("manager");

        boolean result = systemRoleAccessChecker.hasAccess(ManagerOrAdminView.class);

        assertTrue(result);
    }

    static class ViewWithoutAnnotation {
    }

    @RequiredSystemRoles(SystemRole.SYSTEM_ADMIN)
    static class AdminOnlyView {
    }

    @RequiredSystemRoles({SystemRole.SYSTEM_ADMIN, SystemRole.MANAGER})
    static class ManagerOrAdminView {
    }
}