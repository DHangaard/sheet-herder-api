package app.dtos;

import app.enums.LanguageType;

import java.util.List;

public record LanguageDTO(
        Long id,
        String name,
        String description,
        LanguageType type,
        List<String> typicalSpeakers,
        String script
)
{
}
