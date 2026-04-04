package app.routes;

import app.controllers.interfaces.IReferenceController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SubraceRoute
{
    private final IReferenceController subraceController;

    public SubraceRoute(IReferenceController subraceController)
    {
        this.subraceController = subraceController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("/subraces", () ->
            {
                get(subraceController::getAll, Role.ANYONE);
                get("/id/{id}", subraceController::getById, Role.ANYONE);
                get("/name/{name}", subraceController::getByName, Role.ANYONE);
            });
        };
    }
}
