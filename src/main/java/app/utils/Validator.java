package app.utils;

import java.util.List;

public final class Validator
{

    private Validator() {}

    public static <T> void notNull(T object)
    {
        if (object == null)
        {
            throw new IllegalArgumentException("Input cannot be null");
        }
    }

    public static void notBlank(String value)
    {
        if (value == null || value.isEmpty())
        {
            throw new IllegalArgumentException("String cannot be null or blank");
        }
    }

    public static <T> void notEmpty(List<T> objects)
    {
        if (objects == null || objects.isEmpty())
        {
            throw new IllegalArgumentException("List cannot be null or empty");
        }
    }

    public static void validId(Long id)
    {
        if (id == null || id <= 0)
        {
            throw new IllegalArgumentException("Invalid ID");
        }
    }
}
