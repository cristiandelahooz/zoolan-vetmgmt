package com.wornux.security;

import com.wornux.data.entity.Employee;
import com.wornux.data.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public record UserDetailsImpl(User user) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getSystemRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name()));
            log.debug("Added system role for user {}: ROLE_{}", user.getUsername(), user.getSystemRole().name());
        }

        if (user instanceof Employee employee) {
            if (employee.getEmployeeRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_EMP_" + employee.getEmployeeRole().name()));
                log.debug("Added employee role for user {}: ROLE_EMP_{}", user.getUsername(),
                        employee.getEmployeeRole().name());
            }
        }

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            log.warn("No roles defined for user {}, defaulting to ROLE_USER", user.getUsername());
        }

        log.debug("Total granted authorities for user {}: {}", user.getUsername(), authorities);
        return authorities;
    }

    @Override
    public String getPassword() {
        log.debug("Password retrieved for user: {} - Hash length: {}", user.getUsername(),
                user.getPassword() != null ? user.getPassword().length() : 0);
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = user.isActive();
        log.debug("User {} enabled status: {}", user.getUsername(), enabled);
        return enabled;
    }
}
