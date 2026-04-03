package app.dtos.domain;

import app.dtos.reference.LanguageDTO;
import app.enums.Ability;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record CharacterSheetDTO(
        Long id,
        Long userId,
        String name,
        Long raceId,
        String raceName,
        Long subraceId,
        String subraceName,
        Set<LanguageDTO> languages,
        Map<Ability, Integer> abilityScores,
        Map<String, String> notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
)
{
}
