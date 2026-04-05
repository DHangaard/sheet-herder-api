package app.services.domain.implementations;

import app.dtos.domain.UpdateUserDTO;
import app.dtos.domain.UserDTO;
import app.exceptions.ConflictException;
import app.exceptions.ForbiddenException;
import app.mappers.DTOMapper;
import app.persistence.daos.domain.interfaces.IUserDAO;
import app.persistence.entities.domain.User;
import app.security.utils.PasswordUtil;
import app.services.domain.interfaces.IUserService;
import app.utils.Validator;

public class UserService implements IUserService
{
    private final IUserDAO userDAO;

    public UserService(IUserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    @Override
    public User getById(Long id)
    {
        Validator.validId(id);
        return userDAO.getById(id);
    }

    @Override
    public UserDTO update(User user, Long id, UpdateUserDTO dto)
    {
        Validator.notNull(dto);
        Validator.validId(id);
        validateOwnership(user, id);
        validateUnique(dto.email(), dto.username(), id);
        User existing = userDAO.getById(id);
        User updatedUser = buildUpdatedUser(existing, dto);

        return DTOMapper.userToUserDTO(
                userDAO.update(updatedUser)
        );
    }

    @Override
    public Long delete(User user, Long id)
    {
        Validator.validId(id);
        userDAO.getById(id); // throws NotFoundException if not found
        validateOwnership(user, id);
        return userDAO.delete(id);
    }

    private void validateUnique(String email, String username, Long excludeId)
    {
        if (email != null)
            userDAO.getByEmail(email)
                    .filter(user -> !user.getId().equals(excludeId))
                    .ifPresent(user ->
                    {
                        throw new ConflictException("The chosen email is not available: " + email);
                    });

        if (username != null)
            userDAO.getByUsername(username)
                    .filter(user -> !user.getId().equals(excludeId))
                    .ifPresent(user ->
                    {
                        throw new ConflictException("The chosen username is not available: " + username);
                    });
    }

    private void validateOwnership(User user, Long targetId)
    {
        if (!user.getId().equals(targetId))
            throw new ForbiddenException("You can only modify your own account");
    }

    private User buildUpdatedUser(User user, UpdateUserDTO dto)
    {
        if (dto.email() != null)
        {
            Validator.validEmail(dto.email());
            user.setEmail(dto.email());
        }

        if (dto.username() != null)
        {
            Validator.validUsername(dto.username());
            user.setUsername(dto.username());
        }

        if (dto.password() != null)
        {
            Validator.validPassword(dto.password());
            user.setHashedPassword(PasswordUtil.hashPassword(dto.password()));
        }

        return user;
    }
}
