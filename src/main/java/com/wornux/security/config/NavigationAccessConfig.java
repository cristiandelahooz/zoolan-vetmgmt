package com.wornux.security.config;

import com.vaadin.flow.server.auth.NavigationAccessControlConfigurer;
import com.wornux.security.navigation.SystemRoleAccessChecker;
import com.wornux.security.navigation.TenantAccessChecker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NavigationAccessConfig {

    @Bean
    public static NavigationAccessControlConfigurer navigationAccessControlConfigurer(
            SystemRoleAccessChecker systemRoleAccessChecker,
            TenantAccessChecker tenantAccessChecker) {
        return new NavigationAccessControlConfigurer()
                .withAnnotatedViewAccessChecker()
                .withRoutePathAccessChecker()
                .withNavigationAccessChecker(systemRoleAccessChecker)
                .withNavigationAccessChecker(tenantAccessChecker);
    }
}