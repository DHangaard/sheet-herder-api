package app.services.reference;

import app.dtos.RaceDTO;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.entities.reference.Language;
import app.entities.reference.Race;
import app.entities.reference.Trait;
import app.enums.Size;
import app.mappers.DTOMapper;
import app.persistence.IReferenceDAO;
import app.utils.Validator;

import java.util.List;
import java.util.Set;
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
        return dtos.stream()
                .map(dto -> raceDAO.create(buildRace(dto)))
                .map(DTOMapper::raceToDTO)
                .toList();
    }

    @Override
    public RaceDTO getById(Long id)
    {
        Validator.validId(id);
        Race race = raceDAO.getById(id);
        return DTOMapper.raceToDTO(race);
    }

    @Override
    public RaceDTO getByName(String name)
    {
        Validator.notBlank(name);
        Race race = raceDAO.getByName(name);
        return DTOMapper.raceToDTO(race);
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
    public RaceDTO update(DNDRaceDetailDTO dto)
    {
        Validator.notNull(dto);
        Race race = raceDAO.update(buildRace(dto));
        return DTOMapper.raceToDTO(race);
    }

    @Override
    public List<RaceDTO> updateAll(List<DNDRaceDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);
        return dtos.stream()
                .map(dto -> raceDAO.update(buildRace(dto)))
                .map(DTOMapper::raceToDTO)
                .toList();
    }

    @Override
    public Long delete(Long id)
    {
        Validator.validId(id);
        return raceDAO.delete(id);
    }

    private Race buildRace(DNDRaceDetailDTO dto)
    {
        Set<Language> languages = dto.languages().stream()
                .map(language -> languageDAO.getByName(language.name()))
                .collect(Collectors.toSet());

        Set<Trait> traits = dto.traits().stream()
                .map(trait -> traitDAO.getByName(trait.name()))
                .collect(Collectors.toSet());

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
                traits
        );
    }
}
