package app.routes;

import app.controllers.interfaces.ICharacterSheetController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CharacterSheetRoute
{
    private final ICharacterSheetController characterSheetController;

    public CharacterSheetRoute(ICharacterSheetController characterSheetController)
    {
        this.characterSheetController = characterSheetController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("character-sheets", () ->
            {
                get(characterSheetController::getAllByUser, Role.USER);
                get("{id}", characterSheetController::getById, Role.USER);
                post("", characterSheetController::create, Role.USER);
                put("{id}", characterSheetController::update, Role.USER);
                delete("{id}", characterSheetController::delete, Role.USER);
            });
        };
    }
}
