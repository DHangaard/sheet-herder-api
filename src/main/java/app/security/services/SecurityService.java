package app.security.services;

import app.exceptions.ConflictException;
import app.exceptions.TokenCreationException;
import app.exceptions.UnauthorizedException;
import app.mappers.DTOMapper;
import app.persistence.entities.domain.User;
import app.security.daos.IUserDAO;
import app.security.dtos.LoginRequestDTO;
import app.security.dtos.RegisterRequestDTO;
import app.security.dtos.UserDTO;
import app.security.utils.JWTUtil;
import app.security.utils.PasswordUtil;
import app.utils.Validator;
import com.nimbusds.jose.JOSEException;

public class SecurityService implements ISecurityService
{
    private final IUserDAO userDAO;

    public SecurityService(IUserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    @Override
    public UserDTO register(RegisterRequestDTO request)
    {
        Validator.validEmail(request.email());
        Validator.validUsername(request.username());
        Validator.validPassword(request.password());

        userDAO.getByEmail(request.email()).ifPresent(user -> {
            throw new ConflictException("The chosen email is not available: " + request.email());
        });

        String hashedPassword = PasswordUtil.hashPassword(request.password());
        User user = userDAO.create(new User(request.email(), request.username(), hashedPassword));
        return DTOMapper.userToDTO(user);
    }

    @Override
    public String login(LoginRequestDTO request)
    {
        Validator.validEmail(request.email());
        Validator.notNullOrBlank(request.password());

        User user = userDAO.getByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!PasswordUtil.verifyPassword(request.password(), user.getHashedPassword()))
        {
            throw new UnauthorizedException("Invalid credentials");
        }

        try
        {
            return JWTUtil.createToken(user.getUsername(), user.getRoles());
        }
        catch (JOSEException e)
        {
            throw new TokenCreationException("Failed to create token", e);
        }
    }
}
