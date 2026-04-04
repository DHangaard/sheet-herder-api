package app.services.domain.interfaces;

import app.dtos.domain.CharacterSheetDTO;
import app.dtos.domain.CreateCharacterSheetDTO;
import app.dtos.domain.UpdateCharacterSheetDTO;
import app.persistence.entities.domain.User;

import java.util.List;

public interface ICharacterSheetService
{
    CharacterSheetDTO create(User user, CreateCharacterSheetDTO dto);
    CharacterSheetDTO update(User user, UpdateCharacterSheetDTO dto);
    Long delete(User user, Long id);
    CharacterSheetDTO getById(User user, Long id);
    List<CharacterSheetDTO> findAllByUser(User user);
}
