package app.dtos.reference;

import java.util.List;

public record TraitDTO(
        Long id,
        String name,
        List<String> descriptions
)
{
}
