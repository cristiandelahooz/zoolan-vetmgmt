package com.wornux.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Base system roles for authentication and authorization
 */
@Getter
@RequiredArgsConstructor
public enum SystemRole {
    SYSTEM_ADMIN("System Administrator", "Full system access"),
    MANAGER("Manager", "Business management access"),
    USER("User", "Basic system access"),
    GUEST("Guest", "Limited access");

    private final String displayName;
    private final String description;
}