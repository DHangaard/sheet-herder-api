package app.utils;

import app.exceptions.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest
{
    @Nested
    @DisplayName("notNull")
    class NotNull
    {
        @Test
        @DisplayName("Should throw when value is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.notNull(null));
        }

        @Test
        @DisplayName("Should not throw when value is present")
        void passesOnValue()
        {
            assertDoesNotThrow(() -> Validator.notNull("hello"));
        }
    }

    @Nested
    @DisplayName("notNullOrBlank")
    class NotNullOrBlank
    {
        @Test
        @DisplayName("Should throw when value is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.notNullOrBlank(null));
        }

        @Test
        @DisplayName("Should throw when value is blank")
        void throwsOnBlank()
        {
            assertThrows(ValidationException.class, () -> Validator.notNullOrBlank("   "));
        }

        @Test
        @DisplayName("Should not throw when value is present")
        void passesOnValue()
        {
            assertDoesNotThrow(() -> Validator.notNullOrBlank("hello"));
        }
    }

    @Nested
    @DisplayName("notBlank")
    class NotBlank
    {
        @Test
        @DisplayName("Should throw when value is blank")
        void throwsOnBlank()
        {
            assertThrows(ValidationException.class, () -> Validator.notBlank("   "));
        }

        @Test
        @DisplayName("Should not throw when value is null")
        void passesOnNull()
        {
            assertDoesNotThrow(() -> Validator.notBlank(null));
        }

        @Test
        @DisplayName("Should not throw when value is present")
        void passesOnValue()
        {
            assertDoesNotThrow(() -> Validator.notBlank("hello"));
        }
    }

    @Nested
    @DisplayName("notEmpty (List)")
    class NotEmptyList
    {
        @Test
        @DisplayName("Should throw when list is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.notEmpty((List<Object>) null));
        }

        @Test
        @DisplayName("Should throw when list is empty")
        void throwsOnEmpty()
        {
            assertThrows(ValidationException.class, () -> Validator.notEmpty(List.of()));
        }

        @Test
        @DisplayName("Should not throw when list has elements")
        void passesOnNonEmpty()
        {
            assertDoesNotThrow(() -> Validator.notEmpty(List.of("element")));
        }
    }

    @Nested
    @DisplayName("notEmpty (Set)")
    class NotEmptySet
    {
        @Test
        @DisplayName("Should throw when set is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.notEmpty((Set<Object>) null));
        }

        @Test
        @DisplayName("Should throw when set is empty")
        void throwsOnEmpty()
        {
            assertThrows(ValidationException.class, () -> Validator.notEmpty(Set.of()));
        }

        @Test
        @DisplayName("Should not throw when set has elements")
        void passesOnNonEmpty()
        {
            assertDoesNotThrow(() -> Validator.notEmpty(Set.of("element")));
        }
    }

    @Nested
    @DisplayName("validId")
    class ValidId
    {
        @Test
        @DisplayName("Should throw when id is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.validId(null));
        }

        @Test
        @DisplayName("Should throw when id is zero")
        void throwsOnZero()
        {
            assertThrows(ValidationException.class, () -> Validator.validId(0L));
        }

        @Test
        @DisplayName("Should throw when id is negative")
        void throwsOnNegative()
        {
            assertThrows(ValidationException.class, () -> Validator.validId(-1L));
        }

        @Test
        @DisplayName("Should not throw when id is positive")
        void passesOnPositive()
        {
            assertDoesNotThrow(() -> Validator.validId(1L));
        }
    }

    @Nested
    @DisplayName("validEmail")
    class ValidEmail
    {
        @Test
        @DisplayName("Should throw when email is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail(null));
        }

        @Test
        @DisplayName("Should throw when email is blank")
        void throwsOnBlank()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("   "));
        }

        @Test
        @DisplayName("Should throw when email has no @")
        void throwsOnMissingAt()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("invalidemail.com"));
        }

        @Test
        @DisplayName("Should throw when email has no domain")
        void throwsOnMissingDomain()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("invalid@"));
        }

        @Test
        @DisplayName("Should throw when email has no TLD")
        void throwsOnNoTLD()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("user@domain"));
        }

        @Test
        @DisplayName("Should throw when TLD is only one character")
        void throwsOnSingleCharTLD()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("user@domain.c"));
        }

        @Test
        @DisplayName("Should throw when email has spaces")
        void throwsOnSpaces()
        {
            assertThrows(ValidationException.class, () -> Validator.validEmail("user @domain.com"));
        }

        @Test
        @DisplayName("Should not throw on valid email")
        void passesOnValid()
        {
            assertDoesNotThrow(() -> Validator.validEmail("user@example.com"));
        }

        @Test
        @DisplayName("Should not throw on email with subdomain")
        void passesOnSubdomain()
        {
            assertDoesNotThrow(() -> Validator.validEmail("user@mail.example.com"));
        }

        @Test
        @DisplayName("Should not throw on email with plus addressing")
        void passesOnPlusAddressing()
        {
            assertDoesNotThrow(() -> Validator.validEmail("user+tag@example.com"));
        }

        @Test
        @DisplayName("Should not throw on email with dots in local part")
        void passesOnDotsInLocalPart()
        {
            assertDoesNotThrow(() -> Validator.validEmail("first.last@example.com"));
        }
    }

    @Nested
    @DisplayName("validUsername")
    class ValidUsername
    {
        @Test
        @DisplayName("Should throw when username is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.validUsername(null));
        }

        @Test
        @DisplayName("Should throw when username is too short")
        void throwsOnTooShort()
        {
            assertThrows(ValidationException.class, () -> Validator.validUsername("ab"));
        }

        @Test
        @DisplayName("Should throw when username contains invalid characters")
        void throwsOnInvalidChars()
        {
            assertThrows(ValidationException.class, () -> Validator.validUsername("invalid user!"));
        }

        @Test
        @DisplayName("Should not throw on valid username")
        void passesOnValid()
        {
            assertDoesNotThrow(() -> Validator.validUsername("valid_user-1"));
        }
    }

    @Nested
    @DisplayName("validPassword")
    class ValidPassword
    {
        @Test
        @DisplayName("Should throw when password is null")
        void throwsOnNull()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword(null));
        }

        @Test
        @DisplayName("Should throw when password has no uppercase")
        void throwsOnNoUppercase()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword("password1!"));
        }

        @Test
        @DisplayName("Should throw when password has no lowercase")
        void throwsOnNoLowercase()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword("PASSWORD1!"));
        }

        @Test
        @DisplayName("Should throw when password has no digit")
        void throwsOnNoDigit()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword("Password!"));
        }

        @Test
        @DisplayName("Should throw when password has no special character")
        void throwsOnNoSpecialChar()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword("Password1"));
        }

        @Test
        @DisplayName("Should throw when password is too short")
        void throwsOnTooShort()
        {
            assertThrows(ValidationException.class, () -> Validator.validPassword("Pa1!"));
        }

        @Test
        @DisplayName("Should not throw on valid password")
        void passesOnValid()
        {
            assertDoesNotThrow(() -> Validator.validPassword("Password_1"));
        }
    }
}