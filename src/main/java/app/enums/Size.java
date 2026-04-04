package app.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Size
{
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String value;

    Size(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @JsonCreator
    public static Size fromValue(String value)
    {
        for (Size size : values())
        {
            if (size.value.equals(value))
            {
                return size;
            }
        }
        throw new IllegalArgumentException("Unknown size type value: " + value);
    }
}
