package app.services.reference.implementations;

import app.dtos.reference.SubraceDTO;
import app.dtos.dnd.DNDSubraceDetailDTO;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
import app.persistence.entities.reference.Trait;
import app.mappers.DTOMapper;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.services.reference.interfaces.IReferenceDataService;
import app.utils.ContentHashing;
import app.utils.Validator;

import java.util.*;
import java.util.stream.Collectors;

public class SubraceService implements IReferenceDataService<DNDSubraceDetailDTO, SubraceDTO>
{
    private final IReferenceDAO<Subrace> subraceDAO;
    private final IReferenceDAO<Race> raceDAO;
    private final IReferenceDAO<Trait> traitDAO;

    public SubraceService(IReferenceDAO<Subrace> subraceDAO, IReferenceDAO<Race> raceDAO, IReferenceDAO<Trait> traitDAO)
    {
        this.subraceDAO = subraceDAO;
        this.raceDAO = raceDAO;
        this.traitDAO = traitDAO;
    }

    @Override
    public List<SubraceDTO> persistAll(List<DNDSubraceDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);

        Set<String> incomingRaceNames = dtos.stream()
                .map(dto -> ContentHashing.normalizeLower(dto.race().name()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        Set<String> incomingTraitNames = dtos.stream()
                .flatMap(dto -> dto.traits().stream())
                .map(trait -> ContentHashing.normalizeLower(trait.name()))
                .filter(name -> !name.isBlank())
                .collect(Collectors.toSet());

        Map<String, Race> racesByName = raceDAO.getByNames(incomingRaceNames).stream()
                .collect(Collectors.toMap(
                        race -> ContentHashing.normalizeLower(race.getName()),
                        race -> race
                ));

        Map<String, Trait> traitsByName = traitDAO.getByNames(incomingTraitNames).stream()
                .collect(Collectors.toMap(
                        trait -> ContentHashing.normalizeLower(trait.getName()),
                        trait -> trait
                ));

        List<Subrace> subraces = dtos.stream()
                .map(dto -> buildSubrace(dto, racesByName, traitsByName))
                .toList();

        return subraceDAO.syncAll(subraces).stream()
                .map(DTOMapper::subraceToDTO)
                .toList();
    }

    @Override
    public Optional<SubraceDTO> getById(Long id)
    {
        Validator.validId(id);
        Subrace subrace = subraceDAO.getById(id);
        return Optional.ofNullable(DTOMapper.subraceToDTO(subrace));
    }

    @Override
    public Optional<SubraceDTO> getByName(String name)
    {
        Validator.notNullOrBlank(name);
        Subrace subrace = subraceDAO.getByName(name);
        return Optional.ofNullable(DTOMapper.subraceToDTO(subrace));
    }

    @Override
    public List<SubraceDTO> getAll()
    {
        return subraceDAO.getAll()
                .stream()
                .map(DTOMapper::subraceToDTO)
                .toList();
    }

    @Override
    public Long delete(Long id)
    {
        Validator.validId(id);
        return subraceDAO.delete(id);
    }

    private Subrace buildSubrace(DNDSubraceDetailDTO dto, Map<String, Race> racesByName, Map<String, Trait> traitsByName)
    {
        Race race = racesByName.get(ContentHashing.normalizeLower(dto.race().name()));
        if (race == null)
        {
            throw new IllegalStateException("Race not found while building Subrace: " + dto.race().name());
        }

        Set<Trait> traits = dto.traits().stream()
                .map(trait -> ContentHashing.normalizeLower(trait.name()))
                .map(traitsByName::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return new Subrace(
                dto.name(),
                dto.description(),
                race,
                DTOMapper.toAbilityBonusMap(dto.abilityBonuses()),
                traits,
                ContentHashing.sha256Hex(buildSubraceHashMaterial(dto, race, traits))
        );
    }

    private String buildSubraceHashMaterial(DNDSubraceDetailDTO dto, Race race, Set<Trait> traits)
    {
        return String.join("|",
                ContentHashing.normalizeLower(dto.name()),
                ContentHashing.normalize(dto.description()),
                ContentHashing.normalizeLower(race.getName()),
                ContentHashing.joinSortedEnumMap(DTOMapper.toAbilityBonusMap(dto.abilityBonuses())),
                ContentHashing.joinSortedMapped(traits, Trait::getName)
        );
    }
}
