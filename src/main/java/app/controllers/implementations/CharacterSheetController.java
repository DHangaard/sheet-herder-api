package app.controllers.implementations;

import app.controllers.ControllerConstants;
import app.controllers.interfaces.ICharacterSheetController;
import app.dtos.domain.CharacterSheetDTO;
import app.dtos.domain.CreateCharacterSheetDTO;
import app.dtos.domain.UpdateCharacterSheetDTO;
import app.exceptions.UnauthorizedException;
import app.persistence.entities.domain.User;
import app.security.dtos.UserSecurityDTO;
import app.services.domain.interfaces.ICharacterSheetService;
import app.services.domain.interfaces.IUserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class CharacterSheetController implements ICharacterSheetController
{
    private static final String USER_ATTRIBUTE = ControllerConstants.USER_ATTRIBUTE;
    private final ICharacterSheetService characterSheetService;
    private final IUserService userService;

    public CharacterSheetController(ICharacterSheetService characterSheetService, IUserService userService)
    {
        this.characterSheetService = characterSheetService;
        this.userService = userService;
    }

    @Override
    public void create(Context ctx)
    {
        User user = resolveUser(ctx);
        CreateCharacterSheetDTO createCharacterSheetDTO = ctx.bodyAsClass(CreateCharacterSheetDTO.class);
        CharacterSheetDTO characterSheetDTO = characterSheetService.create(user, createCharacterSheetDTO);
        ctx.status(HttpStatus.CREATED).json(characterSheetDTO);
    }

    @Override
    public void getById(Context ctx)
    {
        User user = resolveUser(ctx);
        Long id = Long.parseLong(ctx.pathParam("id"));
        CharacterSheetDTO characterSheetDTO = characterSheetService.getById(user, id);
        ctx.status(HttpStatus.OK).json(characterSheetDTO);
    }

    @Override
    public void update(Context ctx)
    {
        User user = resolveUser(ctx);
        UpdateCharacterSheetDTO updateCharacterSheetDTO = ctx.bodyAsClass(UpdateCharacterSheetDTO.class);
        CharacterSheetDTO characterSheetDTO = characterSheetService.update(user, updateCharacterSheetDTO);
        ctx.status(HttpStatus.OK).json(characterSheetDTO);
    }

    @Override
    public void delete(Context ctx)
    {
        User user = resolveUser(ctx);
        Long id = Long.parseLong(ctx.pathParam("id"));
        characterSheetService.delete(user, id);
        ctx.status(HttpStatus.NO_CONTENT);
    }

    @Override
    public void findAllByUser(Context ctx)
    {
        User user = resolveUser(ctx);
        List<CharacterSheetDTO> characterSheetDTOs = characterSheetService.findAllByUser(user);
        ctx.status(HttpStatus.OK).json(characterSheetDTOs);
    }

    private User resolveUser(Context ctx)
    {
        UserSecurityDTO userSecurityDTO = ctx.attribute(USER_ATTRIBUTE);
        if (userSecurityDTO == null)
        {
            throw new UnauthorizedException("No authenticated user on request");
        }
        return userService.getById(userSecurityDTO.id());
    }
}
