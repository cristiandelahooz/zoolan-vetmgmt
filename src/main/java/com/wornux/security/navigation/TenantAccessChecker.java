package com.wornux.security.navigation;

import com.wornux.security.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantAccessChecker {

    private final SecurityContextService securityContextService;

    public boolean hasAccess(Class<?> targetClass) {
        if (!securityContextService.isAuthenticated()) {
            return true;
        }

        return true;
    }
}