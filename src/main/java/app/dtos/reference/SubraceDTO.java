package app.dtos.reference;

import app.enums.Ability;

import java.util.Map;
import java.util.Set;

public record SubraceDTO(
        Long id,
        String name,
        String description,
        RaceDTO race,
        Map<Ability, Integer> abilityBonuses,
        Set<TraitDTO> traits
)
{
}
