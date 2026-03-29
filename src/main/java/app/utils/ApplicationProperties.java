
package app.utils;

import app.exceptions.PropertyException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ApplicationProperties
{
    private static final Properties PROPERTIES = new Properties();
    private static final String NOT_FOUND = "The variable %s is not present in config.properties";
    private static final String EMPTY = "The variable %s in config.properties has no value";

    static
    {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"))
        {
            if (inputStream == null)
            {
                throw new PropertyException("config.properties not found classpath (e.g. src/main/resources/config.properties)");
            }
            PROPERTIES.load(inputStream);
        }
        catch (IOException e)
        {
            throw new PropertyException("Error when reading config.properties: " + e.getMessage());
        }
    }

    private ApplicationProperties()
    {
    }

    public static String getRequired(String key)
    {
        return getTrimmedProperty(key);
    }

    private static String getTrimmedProperty(String key)
    {
        String property = PROPERTIES.getProperty(key);

        if (property == null)
        {
            throw new PropertyException(String.format(NOT_FOUND, key));
        }

        property = property.trim();

        if (property.isEmpty())
        {
            throw new PropertyException(String.format(EMPTY, key));
        }
        return property;
    }
}
