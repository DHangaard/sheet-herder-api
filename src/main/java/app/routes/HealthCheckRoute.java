package app.routes;

import app.controllers.interfaces.IHealthCheckController;
import app.security.enums.Role;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class HealthCheckRoute
{
    private final IHealthCheckController healthCheckController;

    public HealthCheckRoute(IHealthCheckController healthCheckController)
    {
        this.healthCheckController = healthCheckController;
    }

    public EndpointGroup getRoutes()
    {
        return () -> get("health-check", healthCheckController::healthCheck, Role.ANYONE);
    }
}
