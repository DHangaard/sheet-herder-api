package app.routes;

import app.controllers.interfaces.IReferenceController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class TraitRoute
{
    private final IReferenceController traitController;

    public TraitRoute(IReferenceController traitController)
    {
        this.traitController = traitController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("/traits", () ->
            {
                get(traitController::getAll, Role.ANYONE);
                get("/id/{id}", traitController::getById, Role.ANYONE);
                get("/name/{name}", traitController::getByName, Role.ANYONE);
            });
        };
    }
}
