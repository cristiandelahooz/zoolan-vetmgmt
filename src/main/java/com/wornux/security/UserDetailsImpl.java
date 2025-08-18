package com.wornux.security;

import com.wornux.data.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public record UserDetailsImpl(User user) implements UserDetails {

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    log.debug("Granted authorities for user {}: {}", user.getUsername(), authorities);
    return authorities;
  }

  @Override
  public String getPassword() {
    log.debug("Password retrieved for user: {} - Hash length: {}", 
        user.getUsername(), user.getPassword() != null ? user.getPassword().length() : 0);
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
