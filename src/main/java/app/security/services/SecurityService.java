package app.security.services;

import app.exceptions.ConflictException;
import app.exceptions.UnauthorizedException;
import app.mappers.DTOMapper;
import app.persistence.entities.domain.User;
import app.persistence.daos.domain.interfaces.IUserDAO;
import app.security.dtos.LoginRequestDTO;
import app.security.dtos.RegisterRequestDTO;
import app.security.dtos.UserSecurityDTO;
import app.security.utils.JWTUtil;
import app.security.utils.PasswordUtil;
import app.utils.Validator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityService implements ISecurityService
{
    private final IUserDAO userDAO;

    public SecurityService(IUserDAO userDAO)
    {
        this.userDAO = userDAO;
    }

    @Override
    public UserSecurityDTO register(RegisterRequestDTO request)
    {
        Validator.validEmail(request.email());
        Validator.validUsername(request.username());
        Validator.validPassword(request.password());

        userDAO.getByEmail(request.email()).ifPresent(user -> {
            throw new ConflictException("The chosen email is not available: " + request.email());
        });

        String hashedPassword = PasswordUtil.hashPassword(request.password());
        User user = userDAO.create(new User(request.email(), request.username(), hashedPassword));
        UserSecurityDTO dto = DTOMapper.userToUserSecurityDTO(user);
        log.info("User registered: {}", user.getEmail());
        return dto;
    }

    @Override
    public String login(LoginRequestDTO request)
    {
        Validator.validEmail(request.email());
        Validator.notNullOrBlank(request.password());

        User user = userDAO.getByEmail(request.email()).orElseThrow(() -> {
            log.warn("Failed login attempt for: {}", request.email());
            return new UnauthorizedException("Invalid credentials");
        });

        if (!PasswordUtil.verifyPassword(request.password(), user.getHashedPassword()))
        {
            log.warn("Failed login attempt for: {}", user.getEmail());
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = JWTUtil.createToken(user.getId(), user.getUsername(), user.getRoles());
        log.info("User logged in: {}", user.getEmail());
        return token;
    }
}
