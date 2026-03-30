package app.config;

import app.exceptions.ApiException;
import app.routes.*;
import app.security.enums.Role;
import app.security.routes.SecurityRoute;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import java.util.Map;

public class ApplicationConfig
{
    public static Javalin startServer(int port)
    {
        DIContainer diContainer = DIContainer.getInstance();
        DataSeeder.seed(diContainer);
        Routes routes = buildRoutes(diContainer);

        Javalin app = Javalin.create(config ->
        {
            configureRoutes(config, routes);
            configureSecurity(config, diContainer);
            configureExceptions(config);
        }).start(port);

        return app;
    }

    public static void stopServer(Javalin app)
    {
        app.stop();
    }

    private static Routes buildRoutes(DIContainer diContainer)
    {
        return new Routes(
                new LanguageRoute(diContainer.getLanguageController()),
                new TraitRoute(diContainer.getTraitController()),
                new RaceRoute(diContainer.getRaceController()),
                new SubraceRoute(diContainer.getSubraceController()),
                new SecurityRoute(diContainer.getSecurityController())
        );
    }

    private static void configureRoutes(JavalinConfig config, Routes routes)
    {
        config.bundledPlugins.enableRouteOverview("/routes", Role.ANYONE);
        config.routes.apiBuilder(routes.getRoutes());
    }

    private static void configureSecurity(JavalinConfig config, DIContainer diContainer)
    {
        config.routes.beforeMatched(diContainer.getSecurityController()::authenticate);
        config.routes.beforeMatched(diContainer.getSecurityController()::authorize);
    }

    private static void configureExceptions(JavalinConfig config)
    {
        config.routes.exception(ApiException.class, (e, ctx) ->
                ctx.status(e.getCode()).json(Map.of("status", e.getCode(), "message", e.getMessage())));
    }
}