package app.services.domain.implementations;

import app.dtos.domain.CharacterSheetDTO;
import app.dtos.domain.CreateCharacterSheetDTO;
import app.dtos.domain.UpdateCharacterSheetDTO;
import app.exceptions.ConflictException;
import app.exceptions.ForbiddenException;
import app.exceptions.ValidationException;
import app.mappers.DTOMapper;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
import app.services.domain.interfaces.ICharacterSheetService;
import app.utils.Validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterSheetService implements ICharacterSheetService
{
    private final ICharacterSheetDAO characterSheetDAO;
    private final IReferenceDAO<Race> raceDAO;
    private final IReferenceDAO<Subrace> subraceDAO;
    private final IReferenceDAO<Language> languageDAO;

    public CharacterSheetService(
            ICharacterSheetDAO characterSheetDAO,
            IReferenceDAO<Race> raceDAO,
            IReferenceDAO<Subrace> subraceDAO,
            IReferenceDAO<Language> languageDAO
    )
    {
        this.characterSheetDAO = characterSheetDAO;
        this.raceDAO = raceDAO;
        this.subraceDAO = subraceDAO;
        this.languageDAO = languageDAO;
    }

    @Override
    public CharacterSheetDTO create(User user, CreateCharacterSheetDTO dto)
    {
        Validator.notNull(user);
        Validator.validId(user.getId());
        Validator.notNull(dto);
        Validator.notNullOrBlank(dto.name());
        validateUniqueName(user, dto.name());
        CharacterSheet characterSheet = buildCharacterSheet(user, dto);

        return DTOMapper.characterSheetToDTO(
                characterSheetDAO.create(characterSheet)
        );
    }

    @Override
    public CharacterSheetDTO getById(User user, Long id)
    {
        Validator.notNull(user);
        Validator.validId(id);
        CharacterSheet characterSheet = characterSheetDAO.getById(id);
        validateOwnership(user, characterSheet);

        return DTOMapper.characterSheetToDTO(characterSheet);
    }

    @Override
    public CharacterSheetDTO update(User user, UpdateCharacterSheetDTO dto)
    {
        Validator.notNull(user);
        Validator.notNull(dto);
        Validator.validId(dto.id());
        validateUniqueName(user, dto.name(), dto.id());
        CharacterSheet characterSheet = characterSheetDAO.getById(dto.id());
        validateOwnership(user, characterSheet);
        CharacterSheet updatedCharacterSheet = buildUpdatedCharacterSheet(characterSheet, dto);

        return DTOMapper.characterSheetToDTO(
                characterSheetDAO.update(updatedCharacterSheet)
        );
    }

    @Override
    public Long delete(User user, Long id)
    {
        Validator.validId(id);
        CharacterSheet characterSheet = characterSheetDAO.getById(id);
        validateOwnership(user, characterSheet);
        return characterSheetDAO.delete(id);
    }

    @Override
    public List<CharacterSheetDTO> findAllByUser(User user)
    {
        Validator.notNull(user);
        return characterSheetDAO.findAllByUser(user)
                .stream()
                .map(DTOMapper::characterSheetToDTO)
                .toList();
    }

    private CharacterSheet buildCharacterSheet(User user, CreateCharacterSheetDTO dto)
    {
        Race race = dto.raceId() != null ? raceDAO.getById(dto.raceId()) : null;
        Subrace subrace = dto.subraceId() != null ? subraceDAO.getById(dto.subraceId()) : null;
        validateSubrace(race, subrace);

        Set<Language> languages = mapIdsToLanguages(dto.languageIds());

        return new CharacterSheet(
                user,
                dto.name(),
                race,
                subrace,
                languages,
                dto.abilityScores()
        );
    }

    private CharacterSheet buildUpdatedCharacterSheet(CharacterSheet characterSheet, UpdateCharacterSheetDTO dto)
    {
        if (dto.name() != null && !dto.name().isBlank())
        {
            characterSheet.setName(dto.name());
        }

        Race race = null;
        if (dto.raceId() != null)
        {
            race = raceDAO.getById(dto.raceId());
            characterSheet.setRace(race);
        }

        if (dto.subraceId() != null)
        {
            Subrace subrace = subraceDAO.getById(dto.subraceId());
            validateSubrace(race, subrace);
            characterSheet.setSubrace(subrace);
        }

        if (dto.languageIds() != null && !dto.languageIds().isEmpty())
        {
            Set<Language> languages = mapIdsToLanguages(dto.languageIds());
            characterSheet.setLanguages(languages);
        }

        if (dto.abilityScores() != null && !dto.abilityScores().isEmpty())
        {
            characterSheet.setAbilityScores(dto.abilityScores());
        }

        if (dto.notes() != null && !dto.notes().isEmpty())
        {
            characterSheet.setNotes(dto.notes());
        }

        return characterSheet;
    }

    private void validateSubrace(Race race, Subrace subrace)
    {
        if (subrace != null && race == null)
        {
            throw new ValidationException("A subrace cannot be assigned without a race");
        }
        if (subrace != null && !subrace.getRace().equals(race))
        {
            throw new ValidationException("The assigned subrace must belong to the assigned race");
        }
    }

    private Set<Language> mapIdsToLanguages(Set<Long> languageIds)
    {
        Set<Language> languages = new HashSet<>();
        if (languageIds != null)
        {
            for (Long id : languageIds)
            {
                languages.add(languageDAO.getById(id));
            }
        }
        return languages;
    }

    private void validateOwnership(User user, CharacterSheet characterSheet)
    {
        if (!characterSheet.getUser().equals(user))
        {
            throw new ForbiddenException("Only the owning user is allowed to alter character sheet");
        }
    }

    private void validateUniqueName(User user, String name)
    {
        boolean nameExists = characterSheetDAO.findAllByUser(user)
                .stream()
                .anyMatch(character -> character.getName().equalsIgnoreCase(name));
        if (nameExists)
        {
            throw new ConflictException("You already own a character named: " + name);
        }
    }

    private void validateUniqueName(User user, String name, Long excludeId)
    {
        boolean nameExists = characterSheetDAO.findAllByUser(user)
                .stream()
                .filter(character -> !character.getId().equals(excludeId))
                .anyMatch(character -> character.getName().equalsIgnoreCase(name));
        if (nameExists)
        {
            throw new ConflictException("You already own a character named: " + name);
        }
    }
}
