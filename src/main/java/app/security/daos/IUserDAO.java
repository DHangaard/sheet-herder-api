package app.security.daos;

import app.persistence.entities.domain.User;
import app.security.enums.Role;

import java.util.Optional;

public interface IUserDAO
{
    User create(User user);
    Optional<User> getByEmail(String email);
    User getById(Long id);
    User update(User user);
    User addRole(Long id, Role role);
    User removeRole(Long id, Role role);
    Long delete(Long id);
}
