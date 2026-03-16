package app.services.reference;

import app.dtos.LanguageDTO;
import app.dtos.dnd.DNDLanguageDetailDTO;
import app.entities.reference.Language;
import app.enums.LanguageType;
import app.mappers.DTOMapper;
import app.persistence.IDAO;
import app.persistence.IReferenceDAO;
import app.utils.Validator;

import java.util.List;

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
        return dtos.stream()
                .map(dto -> languageDAO.create(buildLanguage(dto)))
                .map(DTOMapper::languageToDTO)
                .toList();
    }

    @Override
    public LanguageDTO getById(Long id)
    {
        Validator.validId(id);
        Language language = languageDAO.getById(id);
        return DTOMapper.languageToDTO(language);
    }

    @Override
    public LanguageDTO getByName(String name)
    {
        Validator.notBlank(name);
        Language language = languageDAO.getByName(name);
        return DTOMapper.languageToDTO(language);
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
    public LanguageDTO update(DNDLanguageDetailDTO dto)
    {
        Validator.notNull(dto);
        Language language = languageDAO.update(buildLanguage(dto));
        return DTOMapper.languageToDTO(language);
    }

    @Override
    public List<LanguageDTO> updateAll(List<DNDLanguageDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);
        return dtos.stream()
                .map(dto -> languageDAO.update(buildLanguage(dto)))
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
                dto.script()
        );
    }
}
