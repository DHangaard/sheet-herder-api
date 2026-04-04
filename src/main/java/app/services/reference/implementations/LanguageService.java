package app.services.reference.implementations;

import app.dtos.reference.LanguageDTO;
import app.dtos.dnd.DNDLanguageDetailDTO;
import app.persistence.entities.reference.Language;
import app.enums.LanguageType;
import app.mappers.DTOMapper;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.services.reference.interfaces.IReferenceDataService;
import app.utils.ContentHashing;
import app.utils.Validator;

import java.util.List;
import java.util.Optional;

public class LanguageService implements IReferenceDataService<DNDLanguageDetailDTO, LanguageDTO>
{
    private final IReferenceDAO<Language> languageDAO;

    public LanguageService(IReferenceDAO<Language> languageDAO)
    {
        this.languageDAO = languageDAO;
    }

    @Override
    public List<LanguageDTO> persistAll(List<DNDLanguageDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);
        List<Language> languages = dtos.stream()
                .map(this::buildLanguage)
                .toList();

        return languageDAO.syncAll(languages).stream()
                .map(DTOMapper::languageToDTO)
                .toList();
    }

    @Override
    public Optional<LanguageDTO> getById(Long id)
    {
        Validator.validId(id);
        Language language = languageDAO.getById(id);
        return Optional.ofNullable(DTOMapper.languageToDTO(language));
    }

    @Override
    public Optional<LanguageDTO> getByName(String name)
    {
        Validator.notNullOrBlank(name);
        Language language = languageDAO.getByName(name);
        return Optional.ofNullable(DTOMapper.languageToDTO(language));
    }

    @Override
    public List<LanguageDTO> getAll()
    {
        return languageDAO.getAll()
                .stream()
                .map(DTOMapper::languageToDTO)
                .toList();
    }

    @Override
    public Long delete(Long id)
    {
        Validator.validId(id);
        return languageDAO.delete(id);
    }

    private Language buildLanguage(DNDLanguageDetailDTO dto)
    {
        return new Language(
                dto.name(),
                dto.description(),
                LanguageType.fromValue(dto.type()),
                dto.typicalSpeakers(),
                dto.script(),
                ContentHashing.sha256Hex(buildHashMaterial(dto))
        );
    }

    private String buildHashMaterial(DNDLanguageDetailDTO dto)
    {
        return String.join("|",
                ContentHashing.normalizeLower(dto.name()),
                ContentHashing.normalize(dto.description()),
                ContentHashing.normalize(LanguageType.fromValue(dto.type()).name()),
                ContentHashing.joinSorted(dto.typicalSpeakers()),
                ContentHashing.normalize(dto.script())
        );
    }
}
