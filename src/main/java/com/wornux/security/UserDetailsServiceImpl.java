package com.wornux.security;


import com.wornux.data.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.info("Attempting to load user with username: {}", username);
    
    return userRepository.findByUsername(username)
        .map(user -> {
          log.info("User found: {} - Active: {} - SystemRole: {} - Password hash starts with: {}", 
              user.getUsername(), user.isActive(), user.getSystemRole(),
              user.getPassword() != null ? user.getPassword().substring(0, Math.min(10, user.getPassword().length())) : "null");
          return new UserDetailsImpl(user);
        })
        .orElseThrow(() -> {
          log.error("User not found with username: {}", username);
          return new UsernameNotFoundException("User not found with username: " + username);
        });
  }
}