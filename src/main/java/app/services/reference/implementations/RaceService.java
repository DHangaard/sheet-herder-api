package app.services.reference.implementations;

import app.dtos.reference.RaceDTO;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Trait;
import app.enums.Size;
import app.mappers.DTOMapper;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.services.reference.interfaces.IReferenceDataService;
import app.utils.ContentHashing;
import app.utils.Validator;

import java.util.*;
import java.util.stream.Collectors;

public class RaceService implements IReferenceDataService<DNDRaceDetailDTO, RaceDTO>
{
    private final IReferenceDAO<Race> raceDAO;
    private final IReferenceDAO<Language> languageDAO;
    private final IReferenceDAO<Trait> traitDAO;

    public RaceService(IReferenceDAO<Race> raceDAO, IReferenceDAO<Language> languageDAO, IReferenceDAO<Trait> traitDAO)
    {
        this.raceDAO = raceDAO;
        this.languageDAO = languageDAO;
        this.traitDAO = traitDAO;
    }

    @Override
    public List<RaceDTO> persistAll(List<DNDRaceDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);

        Set<String> incomingLanguageNames = dtos.stream()
                .flatMap(dto -> dto.languages().stream())
                .map(language -> ContentHashing.normalizeLower(language.name()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        Set<String> incomingTraitNames = dtos.stream()
                .flatMap(dto -> dto.traits().stream())
                .map(trait -> ContentHashing.normalizeLower(trait.name()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        Map<String, Language> languagesByName = languageDAO.getByNames(incomingLanguageNames).stream()
                .collect(Collectors.toMap(
                        language -> ContentHashing.normalizeLower(language.getName()),
                        language -> language
                ));

        Map<String, Trait> traitsByName = traitDAO.getByNames(incomingTraitNames).stream()
                .collect(Collectors.toMap(
                        trait -> ContentHashing.normalizeLower(trait.getName()),
                        trait -> trait
                ));

        List<Race> races = dtos.stream()
                .map(dto -> buildRace(dto, languagesByName, traitsByName))
                .toList();

        return raceDAO.syncAll(races).stream()
                .map(DTOMapper::raceToDTO)
                .toList();
    }

    @Override
    public Optional<RaceDTO> getById(Long id)
    {
        Validator.validId(id);
        Race race = raceDAO.getById(id);
        return Optional.ofNullable(DTOMapper.raceToDTO(race));
    }

    @Override
    public Optional<RaceDTO> getByName(String name)
    {
        Validator.notNullOrBlank(name);
        Race race = raceDAO.getByName(name);
        return Optional.ofNullable(DTOMapper.raceToDTO(race));
    }

    @Override
    public List<RaceDTO> getAll()
    {
        return raceDAO.getAll()
                .stream()
                .map(DTOMapper::raceToDTO)
                .toList();
    }

    @Override
    public Long delete(Long id)
    {
        Validator.validId(id);
        return raceDAO.delete(id);
    }

    private Race buildRace(DNDRaceDetailDTO dto, Map<String, Language> languagesByName, Map<String, Trait> traitsByName)
    {
        Set<Language> languages = dto.languages().stream()
                .map(l -> ContentHashing.normalizeLower(l.name()))
                .map(languagesByName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Trait> traits = dto.traits().stream()
                .map(t -> ContentHashing.normalizeLower(t.name()))
                .map(traitsByName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String contentHash = ContentHashing.sha256Hex(buildRaceHashMaterial(dto, languages, traits));

        return new Race(
                dto.name(),
                dto.speed(),
                DTOMapper.toAbilityBonusMap(dto.abilityBonuses()),
                dto.ageDescription(),
                dto.alignment(),
                Size.fromValue(dto.size()),
                dto.sizeDescription(),
                languages,
                dto.languageDescription(),
                traits,
                contentHash
        );
    }

    private String buildRaceHashMaterial(DNDRaceDetailDTO dto, Set<Language> languages, Set<Trait> traits)
    {
        return String.join("|",
                ContentHashing.normalizeLower(dto.name()),
                String.valueOf(dto.speed()),
                ContentHashing.joinSortedEnumMap(DTOMapper.toAbilityBonusMap(dto.abilityBonuses())),
                ContentHashing.normalize(dto.ageDescription()),
                ContentHashing.normalize(dto.alignment()),
                ContentHashing.normalize(Size.fromValue(dto.size()).name()),
                ContentHashing.normalize(dto.sizeDescription()),
                ContentHashing.joinSortedMapped(languages, Language::getName),
                ContentHashing.normalize(dto.languageDescription()),
                ContentHashing.joinSortedMapped(traits, Trait::getName)
        );
    }
}
