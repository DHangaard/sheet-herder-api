package app.mappers;

import app.dtos.domain.CharacterSheetDTO;
import app.dtos.domain.UserDTO;
import app.dtos.reference.LanguageDTO;
import app.dtos.reference.RaceDTO;
import app.dtos.reference.SubraceDTO;
import app.dtos.reference.TraitDTO;
import app.dtos.dnd.DNDAbilityBonusDTO;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
import app.persistence.entities.reference.Trait;
import app.enums.Ability;
import app.security.dtos.UserSecurityDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DTOMapper
{
    public static LanguageDTO languageToDTO(Language language)
    {
        return new LanguageDTO(
                language.getId(),
                language.getName(),
                language.getDescription(),
                language.getType(),
                language.getTypicalSpeakers(),
                language.getScript()
        );
    }

    public static TraitDTO traitToDTO(Trait trait)
    {
        return new TraitDTO(
                trait.getId(),
                trait.getName(),
                trait.getDescriptions()
        );
    }

    public static RaceDTO raceToDTO(Race race)
    {
        return new RaceDTO(
                race.getId(),
                race.getName(),
                race.getSpeed(),
                race.getAbilityBonuses(),
                race.getAgeDescription(),
                race.getAlignment(),
                race.getSize(),
                race.getSizeDescription(),
                nullSafeSet(race.getLanguages(), DTOMapper::languageToDTO),
                nullSafeSet(race.getTraits(), DTOMapper::traitToDTO)
        );
    }

    public static SubraceDTO subraceToDTO(Subrace subrace)
    {
        return new SubraceDTO(
                subrace.getId(),
                subrace.getName(),
                subrace.getDescription(),
                DTOMapper.raceToDTO(subrace.getRace()),
                subrace.getAbilityBonuses(),
                nullSafeSet(subrace.getTraits(), DTOMapper::traitToDTO)
        );
    }

    public static Map<Ability, Integer> toAbilityBonusMap(List<DNDAbilityBonusDTO> bonuses)
    {
        return bonuses.stream()
                .collect(Collectors.toMap(
                        bonus -> Ability.fromValue(bonus.dndAbilityScoreDTO().name()),
                        DNDAbilityBonusDTO::bonus
                ));
    }

    public static UserSecurityDTO userToUserSecurityDTO(User user)
    {
        return new UserSecurityDTO(
                user.getId(),
                user.getUsername(),
                user.getRoles()
        );
    }

    public static UserDTO userToUserDTO(User user)
    {
        return new UserDTO(
                user.getEmail(),
                user.getUsername()
        );
    }

    public static CharacterSheetDTO characterSheetToDTO(CharacterSheet characterSheet)
    {
        return new CharacterSheetDTO(
                characterSheet.getId(),
                characterSheet.getUser().getId(),
                characterSheet.getName(),
                characterSheet.getRace() != null ? characterSheet.getRace().getId() : null,
                characterSheet.getRace() != null ? characterSheet.getRace().getName() : null,
                characterSheet.getSubrace() != null ? characterSheet.getSubrace().getId() : null,
                characterSheet.getSubrace() != null ? characterSheet.getSubrace().getName() : null,
                nullSafeSet(characterSheet.getLanguages(), DTOMapper::languageToDTO),
                characterSheet.getAbilityScores(),
                characterSheet.getNotes(),
                characterSheet.getCreatedAt(),
                characterSheet.getUpdatedAt()
                );
    }

    private static <T, R> Set<R> nullSafeSet(Set<T> source, Function<T, R> mapper)
    {
        if (source == null) return Set.of();
        return source.stream()
                .map(mapper)
                .collect(Collectors.toSet());
    }
}


