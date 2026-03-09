package app.enums;

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

    public String getValue()
    {
        return value;
    }

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
