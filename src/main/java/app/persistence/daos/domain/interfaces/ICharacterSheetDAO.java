package app.persistence.daos.domain.interfaces;

import app.persistence.daos.IDAO;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;

import java.util.List;

public interface ICharacterSheetDAO extends IDAO<CharacterSheet>
{
    List<CharacterSheet> getAllByUser(User user);
}
