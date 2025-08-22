package com.wornux.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordHashGeneratorTest {

    private PasswordHashGenerator passwordHashGenerator;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        passwordHashGenerator = new PasswordHashGenerator(passwordEncoder);
    }

    @Test
    void testPasswordEncodingForAllTestPasswords() throws Exception {
        String[] args = { "generate-passwords" };
        String[] testPasswords = { "1234f", "user123", "vet123", "test123" };

        passwordHashGenerator.run(args);

        for (String password : testPasswords) {
            String hash = passwordEncoder.encode(password);
            assertNotNull(hash);
            assertNotEquals(password, hash);
            assertTrue(passwordEncoder.matches(password, hash));
        }
    }

    @Test
    void testLoggingOutputDuringPasswordGeneration() {
        String[] args = { "generate-passwords" };

        assertDoesNotThrow(() -> passwordHashGenerator.run(args));
    }

    @Test
    void testRunWithIncorrectArgument() {
        String[] args = { "invalid-command" };

        assertDoesNotThrow(() -> passwordHashGenerator.run(args));
    }
}
