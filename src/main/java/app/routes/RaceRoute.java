package app.routes;

import app.controllers.interfaces.IReferenceController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;

public class RaceRoute
{
    private final IReferenceController raceController;

    public RaceRoute(IReferenceController raceController)
    {
        this.raceController = raceController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("races", () ->
            {
                get(raceController::getAll, Role.ANYONE);
                get("{id}", raceController::getById, Role.ANYONE);
                get("name/{name}", raceController::getByName, Role.ANYONE);
            });
        };
    }
}
