package app.security.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest
{
    private static final String VALID_PASSWORD = "Password_1";
    private static final int STANDARD_BCRYPT_COST = 12;
    private static final int LOWEST_BCRYPT_COST = 4;
    private static final int ABOVE_STANDARD_BCRYPT_COST = 13;
    private static final int BCRYPT_PASSWORD_LENGTH = 60;

    private String hash;

    @BeforeEach
    void setUp()
    {
        hash = PasswordUtil.hashPassword(VALID_PASSWORD);
    }

    @Nested
    @DisplayName("HashPassword")
    class HashPassword
    {
        @Test
        @DisplayName("Should return a 60 character hash")
        void hashLength()
        {
            assertEquals(BCRYPT_PASSWORD_LENGTH, hash.length());
        }

        @Test
        @DisplayName("Should return a BCrypt hash")
        void bcryptPrefix()
        {
            assertTrue(PasswordUtil.hashPassword(VALID_PASSWORD).startsWith("$2a$"));
        }

        @Test
        @DisplayName("Should use default cost factor")
        void defaultCostFactor()
        {
            assertTrue(PasswordUtil.hashPassword(VALID_PASSWORD).startsWith(String.format("$2a$%02d$", STANDARD_BCRYPT_COST)));
        }

        @Test
        @DisplayName("Should use specified cost factor")
        void specifiedCostFactor()
        {
            assertTrue(PasswordUtil.hashPassword(VALID_PASSWORD, LOWEST_BCRYPT_COST).startsWith(String.format("$2a$%02d$", LOWEST_BCRYPT_COST)));
        }

        @Test
        @DisplayName("Should produce different hashes for same input")
        void differentHashesSameInput()
        {
            String hash1 = PasswordUtil.hashPassword(VALID_PASSWORD);
            String hash2 = PasswordUtil.hashPassword(VALID_PASSWORD);

            assertNotEquals(hash1, hash2);
        }
    }

    @Nested
    @DisplayName("VerifyPassword")
    class VerifyPassword
    {
        @Test
        @DisplayName("Should return true for correct password")
        void correctPassword()
        {
            assertTrue(PasswordUtil.verifyPassword(VALID_PASSWORD, hash));
        }

        @Test
        @DisplayName("Should return false for incorrect password")
        void incorrectPassword()
        {
            assertFalse(PasswordUtil.verifyPassword("WrongPassword_1", hash));
        }

        @Test
        @DisplayName("Should return false when plain password is null")
        void nullPlainPassword()
        {
            assertFalse(PasswordUtil.verifyPassword(null, hash));
        }

        @Test
        @DisplayName("Should return false when plain password is blank")
        void blankPlainPassword()
        {
            assertFalse(PasswordUtil.verifyPassword("   ", hash));
        }

        @Test
        @DisplayName("Should return false when hashed password is null")
        void nullHashedPassword()
        {
            assertFalse(PasswordUtil.verifyPassword(VALID_PASSWORD, null));
        }

        @Test
        @DisplayName("Should return false when hashed password is blank")
        void blankHashedPassword()
        {
            assertFalse(PasswordUtil.verifyPassword(VALID_PASSWORD, "   "));
        }
    }

    @Nested
    @DisplayName("NeedsRehash")
    class NeedsRehash
    {
        @Test
        @DisplayName("Should return true when cost factor is below standard (12)")
        void lowCostNeedsRehash()
        {
            String lowCostHash = PasswordUtil.hashPassword(VALID_PASSWORD, LOWEST_BCRYPT_COST);

            assertTrue(PasswordUtil.needsRehash(lowCostHash));
        }

        @Test
        @DisplayName("Should return false when cost factor is equal to standard (12)")
        void standardCostNoRehash()
        {
            String standardCostHash = PasswordUtil.hashPassword(VALID_PASSWORD, STANDARD_BCRYPT_COST);

            assertFalse(PasswordUtil.needsRehash(standardCostHash));
        }

        @Test
        @DisplayName("Should return false when cost factor is above standard (12)")
        void highCostNoRehash()
        {
            String highCostHash = PasswordUtil.hashPassword(VALID_PASSWORD, ABOVE_STANDARD_BCRYPT_COST);

            assertFalse(PasswordUtil.needsRehash(highCostHash));
        }

        @Test
        @DisplayName("Should return true when hashed password is null")
        void nullReturnsTrue()
        {
            assertTrue(PasswordUtil.needsRehash(null));
        }

        @Test
        @DisplayName("Should return true when hashed password is blank")
        void blankReturnsTrue()
        {
            assertTrue(PasswordUtil.needsRehash("   "));
        }
    }
}