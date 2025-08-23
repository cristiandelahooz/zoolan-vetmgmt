package com.wornux.security;

import com.vaadin.flow.server.VaadinSession;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.User;
import com.wornux.data.enums.EmployeeRole;
import com.wornux.data.enums.SystemRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class UserUtils {

    private UserUtils() {
        // It's not required
    }

    public static void clear() {
        if (Objects.nonNull(VaadinSession.getCurrent())) {
            VaadinSession.getCurrent().setAttribute(User.class, null);
        }
    }

    public static Optional<User> getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication) && authentication.getPrincipal() instanceof UserDetailsImpl(User user)) {
            return Optional.ofNullable(user);
        }
        if (Objects.nonNull(VaadinSession.getCurrent())) {
            return Optional.ofNullable(VaadinSession.getCurrent().getAttribute(User.class));
        }
        return Optional.empty();
    }

    public static String getCurrentUsername() {
        return getUser().map(User::getUsername).orElse("anonymous");
    }

    public static Optional<SystemRole> getCurrentSystemRole() {
        return getUser().map(User::getSystemRole);
    }

    public static Optional<EmployeeRole> getCurrentEmployeeRole() {
        User user = getUser().orElse(null);
        if (user instanceof Employee employee)
            return Optional.ofNullable(employee.getEmployeeRole());

        return Optional.empty();
    }

    public static boolean hasEmployeeRole(EmployeeRole role) {
        return getCurrentEmployeeRole().map(r -> r == role).orElse(false);
    }

    public static boolean hasSystemRole(SystemRole role) {
        return getCurrentSystemRole().map(r -> r == role).orElse(false);
    }
}
