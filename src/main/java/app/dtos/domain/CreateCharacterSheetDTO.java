package app.dtos.domain;

import app.enums.Ability;

import java.util.Map;
import java.util.Set;

public record CreateCharacterSheetDTO(
        String name,
        Long raceId,
        Long subraceId,
        Set<Long> languageIds,
        Map<Ability, Integer> abilityScores
)
{
}
