package app.dtos.reference;

import app.enums.Ability;
import app.enums.Size;

import java.util.Map;
import java.util.Set;

public record RaceDTO(
        Long id,
        String name,
        int speed,
        Map<Ability, Integer> abilityBonuses,
        String ageDescription,
        String alignment,
        Size size,
        String sizeDescription,
        Set<LanguageDTO> languages,
        Set<TraitDTO> traits
)
{
}
