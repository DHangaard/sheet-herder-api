package app.config;

import app.exceptions.ApiException;
import app.routes.*;
import app.security.enums.Role;
import app.security.routes.SecurityRoute;
import app.utils.ExecutionTimer;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ApplicationConfig
{
    public static Javalin startServer(int port)
    {
        ExecutionTimer.start();
        DIContainer diContainer = DIContainer.getInstance();
        DataSeeder.seed(diContainer);
        Routes routes = buildRoutes(diContainer);

        Javalin app = Javalin.create(config ->
        {
            configureRoutes(config, routes);
            configureSecurity(config, diContainer);
            configureExceptions(config);
            configureLogger(config);
        }).start(port);

        ExecutionTimer.finish("SheetHerder ready on port " + port);
        return app;
    }

    public static void stopServer(Javalin app)
    {
        log.info("SheetHerder shutting down");
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
        {
            log.warn("{} {} - {}", ctx.method(), ctx.path(), e.getMessage());
            ctx.status(e.getCode()).json(Map.of("status", e.getCode(), "message", e.getMessage()));
        });

        config.routes.exception(Exception.class, (e, ctx) ->
        {
            log.error("{} {} - Unhandled exception", ctx.method(), ctx.path(), e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR.getCode()).json(Map.of("status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(), "message", "Internal server error"));
        });
    }

    private static void configureLogger(JavalinConfig config)
    {
        config.requestLogger.http((ctx, ms) ->
                log.info("{} {} - {} ({}ms)",
                        ctx.method(),
                        ctx.path(),
                        ctx.status(),
                        ms.longValue()
                )
        );
    }
}