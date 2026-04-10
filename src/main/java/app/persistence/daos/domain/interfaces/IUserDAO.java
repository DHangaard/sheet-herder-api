package app.persistence.daos.domain.interfaces;

import app.persistence.daos.IDAO;
import app.persistence.entities.domain.User;
import app.security.enums.Role;

import java.util.Optional;

public interface IUserDAO extends IDAO<User>
{
    Optional<User> getByEmail(String email);

    Optional<User> getByUsername(String username);

    User addRole(Long id, Role role);

    User removeRole(Long id, Role role);
}
