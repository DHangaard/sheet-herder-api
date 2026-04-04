package app.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Ability
{
    CHARISMA("CHA"),
    CONSTITUTION("CON"),
    DEXTERITY("DEX"),
    INTELLIGENCE("INT"),
    STRENGTH("STR"),
    WISDOM("WIS");

    private final String value;

    Ability(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @JsonCreator
    public static Ability fromValue(String value)
    {
        for (Ability ability : values())
        {
            if (ability.value.equals(value))
            {
                return ability;
            }
        }
        throw new IllegalArgumentException("Unknown ability score value: " + value);
    }
}
