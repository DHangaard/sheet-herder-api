package app.services.domain.interfaces;

import app.dtos.domain.UpdateUserDTO;
import app.dtos.domain.UserDTO;
import app.persistence.entities.domain.User;

public interface IUserService
{
    User getById(Long id);
    UserDTO update(User user, Long id, UpdateUserDTO dto);
    Long delete(User user, Long id);
}
