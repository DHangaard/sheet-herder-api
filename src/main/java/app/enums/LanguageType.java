package app.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LanguageType
{
    STANDARD("Standard"),
    EXOTIC("Exotic");

    private final String value;

    LanguageType(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @JsonCreator
    public static LanguageType fromValue(String value)
    {
        for (LanguageType languageType : values())
        {
            if (languageType.value.equals(value))
            {
                return languageType;
            }
        }
        throw new IllegalArgumentException("Unknown language type value: " + value);
    }
}
