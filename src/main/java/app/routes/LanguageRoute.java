package app.routes;

import app.controllers.interfaces.IReferenceController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class LanguageRoute
{
    private final IReferenceController languageController;

    public LanguageRoute(IReferenceController languageController)
    {
        this.languageController = languageController;
    }

    protected EndpointGroup getRoutes()
    {
        return () ->
        {
            path("/languages", () ->
            {
                get(languageController::getAll, Role.ANYONE);
                get("/id/{id}", languageController::getById, Role.ANYONE);
                get("/name/{name}", languageController::getByName, Role.ANYONE);
            });
        };
    }
}
