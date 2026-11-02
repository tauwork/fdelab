package com.fdelab;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void adminSeedMatches() {
        assertTrue(encoder.matches(
                "admin123",
                "$2b$10$RhJVzjyVrVk27co1ZT0S9u3mev/fFkodeb9fjg7sJSLs4dzqTGh/C"));
    }

    @Test
    void editorSeedMatches() {
        assertTrue(encoder.matches(
                "editor123",
                "$2b$10$Nd876IBejcskGnfUuX9WCe2N4QCKcLTwwQ.36fZmgQ9wuWwOLComO"));
    }
}
