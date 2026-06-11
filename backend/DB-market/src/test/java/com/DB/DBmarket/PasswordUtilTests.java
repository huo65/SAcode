package com.DB.DBmarket;

import com.DB.DBmarket.pojo.utils.PasswordUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTests {
    @Test
    void matchesHashedAndLegacyPasswords() {
        String hashed = PasswordUtil.hash("secret123");

        assertTrue(PasswordUtil.matches("secret123", hashed));
        assertFalse(PasswordUtil.matches("wrong", hashed));
        assertTrue(PasswordUtil.matches("legacy", "legacy"));
    }
}
