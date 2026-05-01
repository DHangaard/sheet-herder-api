package app.controllers.implementations;

import app.controllers.ControllerConstants;
import app.controllers.interfaces.IUserController;
import app.dtos.domain.UpdateUserDTO;
import app.dtos.domain.UserDTO;
import app.exceptions.NotFoundException;
import app.exceptions.UnauthorizedException;
import app.persistence.entities.domain.User;
import app.security.dtos.UserSecurityDTO;
import app.services.domain.interfaces.IUserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class UserController implements IUserController
{
    private static final String USER_ATTRIBUTE = ControllerConstants.USER_ATTRIBUTE;
    private final IUserService userService;

    public UserController(IUserService userService)
    {
        this.userService = userService;
    }

    @Override
    public void update(Context ctx)
    {
        User user = resolveUser(ctx);
        Long id = Long.parseLong(ctx.pathParam("id"));
        UpdateUserDTO updateUserDTO = ctx.bodyAsClass(UpdateUserDTO.class);
        UserDTO userDTO = userService.update(user, id, updateUserDTO);
        ctx.status(HttpStatus.OK).json(userDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        User user = resolveUser(ctx);
        Long id = Long.parseLong(ctx.pathParam("id"));
        userService.delete(user, id);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private User resolveUser(Context ctx)
    {
        UserSecurityDTO userSecurityDTO = ctx.attribute(USER_ATTRIBUTE);
        if (userSecurityDTO == null)
        {
            throw new UnauthorizedException("No authenticated user on request");
        }
        try
        {
            return userService.getById(userSecurityDTO.id());
        }
        catch (NotFoundException e)
        {
            throw new UnauthorizedException("Session is no longer valid", e);
        }
    }
}
