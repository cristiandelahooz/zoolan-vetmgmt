package com.wornux.security.navigation;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationContext;
import com.wornux.data.enums.SystemRole;
import com.wornux.security.annotations.RequiredSystemRoles;
import com.wornux.security.service.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private NavigationContext navigationContext;

    @InjectMocks
    private SystemRoleAccessChecker systemRoleAccessChecker;

    @BeforeEach
    void setUp() {
        when(navigationContext.isErrorHandling()).thenReturn(false);
    }

    @Test
    void check_ShouldReturnNeutral_WhenInErrorHandlingPhase() {
        when(navigationContext.isErrorHandling()).thenReturn(true);

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertEquals(AccessCheckResult.neutral(), result);
    }

    @Test
    void check_ShouldReturnNeutral_WhenNoAnnotationPresent() {
        when(navigationContext.getNavigationTarget()).thenReturn(ViewWithoutAnnotation.class);

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertEquals(AccessCheckResult.neutral(), result);
    }

    @Test
    void check_ShouldDenyAccess_WhenUserNotAuthenticated() {
        when(navigationContext.getNavigationTarget()).thenReturn(AdminOnlyView.class);
        when(securityContextService.isAuthenticated()).thenReturn(false);

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertFalse(result.granted());
        assertEquals("Authentication required", result.reason());
    }

    @Test
    void check_ShouldDenyAccess_WhenUserHasNoRole() {
        when(navigationContext.getNavigationTarget()).thenReturn(AdminOnlyView.class);
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.empty());
        when(securityContextService.getCurrentUsername()).thenReturn("testuser");

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertFalse(result.granted());
        assertEquals("No role assigned", result.reason());
    }

    @Test
    void check_ShouldAllowAccess_WhenUserHasRequiredRole() {
        when(navigationContext.getNavigationTarget()).thenReturn(AdminOnlyView.class);
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.SYSTEM_ADMIN));
        when(securityContextService.getCurrentUsername()).thenReturn("admin");

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertTrue(result.granted());
    }

    @Test
    void check_ShouldDenyAccess_WhenUserLacksRequiredRole() {
        when(navigationContext.getNavigationTarget()).thenReturn(AdminOnlyView.class);
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.USER));
        when(securityContextService.getCurrentUsername()).thenReturn("user");

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertFalse(result.granted());
        assertEquals("Insufficient privileges", result.reason());
    }

    @Test
    void check_ShouldAllowAccess_WhenUserHasOneOfMultipleRequiredRoles() {
        when(navigationContext.getNavigationTarget()).thenReturn(ManagerOrAdminView.class);
        when(securityContextService.isAuthenticated()).thenReturn(true);
        when(securityContextService.getCurrentSystemRole()).thenReturn(Optional.of(SystemRole.MANAGER));
        when(securityContextService.getCurrentUsername()).thenReturn("manager");

        AccessCheckResult result = systemRoleAccessChecker.check(navigationContext);

        assertTrue(result.granted());
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