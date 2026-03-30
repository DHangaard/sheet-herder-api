package app.security.routes;

import app.security.controllers.ISecurityController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class SecurityRoute
{
    private final ISecurityController securityController;

    public SecurityRoute(ISecurityController securityController)
    {
        this.securityController = securityController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> path("/auth", () ->
        {
            post("/register", securityController::register, Role.ANYONE);
            post("/login", securityController::login, Role.ANYONE);
        });
    }
}
