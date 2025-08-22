package com.wornux.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordHashGenerator implements CommandLineRunner {

  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    if (args.length > 0 && "generate-passwords".equals(args[0])) {
      generateTestPasswords();
    }
  }

  private void generateTestPasswords() {
    log.info("=== GENERATING TEST PASSWORD HASHES ===");

    String[] testPasswords = {"1234f", "user123", "vet123", "test123"};

    for (String password : testPasswords) {
      String hash = passwordEncoder.encode(password);
      log.info("Password: {} -> Hash: {}", password, hash);
    }

    log.info("=== END PASSWORD GENERATION ===");
  }
}
