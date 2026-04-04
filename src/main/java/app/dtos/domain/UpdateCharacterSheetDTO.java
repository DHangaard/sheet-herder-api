package app.dtos.domain;

import app.enums.Ability;

import java.util.Map;
import java.util.Set;

public record UpdateCharacterSheetDTO(
        Long id,
        String name,
        Long raceId,
        Long subraceId,
        Set<Long> languageIds,
        Map<Ability, Integer> abilityScores,
        Map<String, String> notes
)
{
}
