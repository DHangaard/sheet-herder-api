package app.services.reference.implementations;

import app.dtos.reference.TraitDTO;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.persistence.entities.reference.Trait;
import app.mappers.DTOMapper;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.services.reference.interfaces.IReferenceDataService;
import app.utils.ContentHashing;
import app.utils.Validator;

import java.util.List;
import java.util.Optional;

public class TraitService implements IReferenceDataService<DNDTraitDetailDTO, TraitDTO>
{
    private final IReferenceDAO<Trait> traitDAO;

    public TraitService(IReferenceDAO<Trait> traitDAO)
    {
        this.traitDAO = traitDAO;
    }

    @Override
    public List<TraitDTO> persistAll(List<DNDTraitDetailDTO> dtos)
    {
        Validator.notEmpty(dtos);
        List<Trait> traits = dtos.stream()
                .map(this::buildTrait)
                .toList();

        return traitDAO.syncAll(traits).stream()
                .map(DTOMapper::traitToDTO)
                .toList();
    }

    @Override
    public Optional<TraitDTO> getById(Long id)
    {
        Validator.validId(id);
        Trait trait = traitDAO.getById(id);
        return Optional.ofNullable(DTOMapper.traitToDTO(trait));
    }

    @Override
    public Optional<TraitDTO> getByName(String name)
    {
        Validator.notNullOrBlank(name);
        Trait trait = traitDAO.getByName(name);
        return Optional.ofNullable(DTOMapper.traitToDTO(trait));
    }

    @Override
    public List<TraitDTO> getAll()
    {
        return traitDAO.getAll()
                .stream()
                .map(DTOMapper::traitToDTO)
                .toList();
    }

    @Override
    public Long delete(Long id)
    {
        Validator.validId(id);
        return traitDAO.delete(id);
    }

    private Trait buildTrait(DNDTraitDetailDTO dto)
    {
        return new Trait(
                dto.name(),
                dto.descriptions(),
                ContentHashing.sha256Hex(buildHashMaterial(dto))
        );
    }

    private String buildHashMaterial(DNDTraitDetailDTO dto)
    {
        return String.join("|",
                ContentHashing.normalizeLower(dto.name()),
                ContentHashing.joinSorted(dto.descriptions())
        );
    }
}
