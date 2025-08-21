# Security Configuration Guide

## Overview
This application uses Vaadin's Navigation Access Control framework integrated with Spring Security for comprehensive security management.

## Architecture Components

### 1. Navigation Access Control
The application uses Vaadin's official Navigation Access Control API which provides:
- Annotation-based view security (`@RolesAllowed`, `@PermitAll`, `@AnonymousAllowed`)
- Path-based security through Spring Security integration
- Custom navigation access checkers for business-specific rules

### 2. Role System

#### System Roles
The application defines four system-level roles:

| Role | Spring Security Name | Description |
|------|---------------------|-------------|
| SYSTEM_ADMIN | ROLE_SYSTEM_ADMIN | Full system access |
| MANAGER | ROLE_MANAGER | Business management access |
| USER | ROLE_USER | Basic system access |
| GUEST | ROLE_GUEST | Limited access |

#### Employee Roles
Business-specific roles that map to system roles:

| Employee Role | Maps to System Role | Description |
|---------------|-------------------|-------------|
| CLINIC_MANAGER | MANAGER | Clinic management |
| VETERINARIAN | USER | Medical services |
| RECEPTIONIST | USER | Front desk operations |
| ADMINISTRATIVE | USER | Administrative tasks |
| GROOMER | USER | Pet grooming services |
| KENNEL_ASSISTANT | USER | Animal care |
| LAB_TECHNICIAN | USER | Laboratory services |

## View Security Configuration

### Using Security Annotations

#### Public Views (No Authentication Required)
```java
@Route("public")
@AnonymousAllowed
public class PublicView extends Div {
}
```

#### Authenticated Users Only
```java
@Route("dashboard")
@PermitAll
public class DashboardView extends Div {
}
```

#### Role-Based Access
```java
@Route("admin")
@RolesAllowed({"ROLE_SYSTEM_ADMIN"})
public class AdminView extends Div {
}
```

#### Multiple Roles
```java
@Route("management")
@RolesAllowed({"ROLE_SYSTEM_ADMIN", "ROLE_MANAGER"})
public class ManagementView extends Div {
}
```

## Path-Based Security

Configure path-based security in `SecurityConfig.java`:

```java
http.authorizeHttpRequests(authorize ->
    authorize
        .requestMatchers("/admin/**").hasRole("SYSTEM_ADMIN")
        .requestMatchers("/management/**").hasAnyRole("SYSTEM_ADMIN", "MANAGER")
        .requestMatchers("/api/**").authenticated()
        .requestMatchers("/public/**").permitAll()
);
```

## Custom Access Checkers

### Creating a Custom Checker
Implement `NavigationAccessChecker` for custom business rules:

```java
@Component
public class CustomAccessChecker implements NavigationAccessChecker {
    @Override
    public AccessCheckResult check(NavigationContext context) {
        if (customBusinessLogic()) {
            return AccessCheckResult.allow();
        }
        return AccessCheckResult.deny("Custom reason");
    }
}
```

### Registering Custom Checkers
Configure in `NavigationAccessConfig.java`:

```java
@Bean
public static NavigationAccessControlConfigurer navigationAccessControlConfigurer(
        CustomAccessChecker customChecker) {
    return new NavigationAccessControlConfigurer()
            .withAnnotatedViewAccessChecker()
            .withRoutePathAccessChecker()
            .withNavigationAccessChecker(customChecker);
}
```

## Testing Security

### Unit Testing Access Control
```java
@Test
void testAccessControl() {
    when(securityContext.getCurrentSystemRole())
        .thenReturn(Optional.of(SystemRole.MANAGER));
    
    AccessCheckResult result = checker.check(navigationContext);
    
    assertTrue(result.granted());
}
```

### Integration Testing
```java
@SpringBootTest
@WithMockUser(roles = "MANAGER")
void testManagerCanAccessView() {
    // Test navigation to protected view
}
```

## Security Best Practices

1. **Default Deny**: Views without security annotations are denied by default
2. **Least Privilege**: Grant minimum necessary permissions
3. **Audit Logging**: All access decisions are logged
4. **Consistent Naming**: Use ROLE_ prefix for Spring Security roles
5. **Test Coverage**: Ensure all protected routes have security tests

## Troubleshooting

### Common Issues

1. **Access Denied Despite Correct Role**
   - Check if user has the role with ROLE_ prefix
   - Verify annotation is on the view class
   - Check for conflicting path matchers

2. **Navigation Not Working**
   - Ensure NavigationAccessControlConfigurer bean is registered
   - Check if VaadinWebSecurity is properly configured
   - Verify Spring Security is enabled

3. **Custom Checker Not Called**
   - Ensure checker is registered as Spring @Component
   - Add to NavigationAccessControlConfigurer
   - Check for error handling phase

## Migration from Legacy System

If migrating from custom security implementation:

1. Replace `@RequiredSystemRoles` with `@RolesAllowed`
2. Remove `BeforeEnterObserver` implementations
3. Update role names to include ROLE_ prefix
4. Remove `AccessControlBeforeEnterListener`
5. Configure `NavigationAccessControlConfigurer`

## Configuration Files

- `SecurityConfig.java` - Spring Security configuration
- `NavigationAccessConfig.java` - Navigation Access Control setup
- `SystemRole.java` - System role definitions
- `navigation/*.java` - Custom navigation checkers