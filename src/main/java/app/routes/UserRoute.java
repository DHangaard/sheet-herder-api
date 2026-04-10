package app.routes;

import app.controllers.interfaces.IUserController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoute
{
    private final IUserController userController;

    public UserRoute(IUserController userController)
    {
        this.userController = userController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("users", () ->
        {
            put("{id}", userController::update, Role.USER);
            delete("{id}", userController::delete, Role.USER);
        });
    }
}
