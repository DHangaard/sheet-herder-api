package app.utils;

import app.exceptions.ValidationException;

import java.util.List;
import java.util.Set;

public final class Validator
{

    private Validator()
    {
    }

    public static <T> void notNull(T object)
    {
        if (object == null)
        {
            throw new ValidationException("Input cannot be null");
        }
    }

    public static void notNullOrBlank(String value)
    {
        if (value == null || value.isBlank())
        {
            throw new ValidationException("String cannot be null or blank");
        }
    }

    public static <T> void notEmpty(List<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            throw new ValidationException("List cannot be null or empty");
        }
    }

    public static <T> void notEmpty(Set<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            throw new ValidationException("Set cannot be null or empty");
        }
    }

    public static void validId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new ValidationException("Invalid ID");
        }
    }

    public static void validEmail(String email)
    {
        if (email == null || email.isBlank())
        {
            throw new ValidationException("Email cannot be blank");
        }

        if (!email.trim().toLowerCase().matches("^[a-z\\d._%+\\-]+@[a-z\\d.\\-]+\\.[a-z]{2,}$"))
        {
            throw new ValidationException("Invalid email format");
        }
    }

    // TODO Add blocklist of profanity words
    public static void validUsername(String username)
    {
        if (username == null || username.isBlank())
        {
            throw new ValidationException("Username cannot be blank");
        }

        String trimmed = username.trim();

        if (trimmed.length() < 3)
        {
            throw new ValidationException("Username must be at least 3 characters");
        }

        if (!trimmed.matches("^[a-zA-Z\\d_\\-]+$"))
        {
            throw new ValidationException("Username can only contain letters, digits, underscores and hyphens");
        }
    }

    public static void validPassword(String password)
    {
        if (password == null || password.isBlank())
        {
            throw new ValidationException("Password cannot be blank");
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\p{Punct}]).{8,}$"))
        {
            throw new ValidationException("Password must contain uppercase, lowercase, digit and special character");
        }
    }
}
