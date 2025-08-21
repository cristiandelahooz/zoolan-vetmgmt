package com.wornux.security.navigation;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import com.wornux.security.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantAccessChecker implements NavigationAccessChecker {

    private final SecurityContextService securityContextService;

    @Override
    public AccessCheckResult check(NavigationContext context) {
        if (context.isErrorHandling()) {
            return AccessCheckResult.neutral();
        }

        if (!securityContextService.isAuthenticated()) {
            return AccessCheckResult.neutral();
        }

        return AccessCheckResult.neutral();
    }
}