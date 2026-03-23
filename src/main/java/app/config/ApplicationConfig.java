package app.config;

import app.routes.*;
import io.javalin.Javalin;

public class ApplicationConfig
{
    public static Javalin startServer(int port)
    {
        ApplicationContext applicationContext = ApplicationContext.getInstance();
        Routes routes = buildRoutes(applicationContext);

        Javalin app = Javalin.create(config ->
        {
            config.bundledPlugins.enableRouteOverview("/routes");
            config.routes.apiBuilder(routes.getRoutes());
            config.routes.exception(RuntimeException.class, (e, ctx) ->
            {
                ctx.status(400).json(e.getMessage());
            });
            config.routes.exception(Exception.class, (e, ctx) ->
            {
               e.printStackTrace();
               ctx.status(500).json(e.getMessage());
            });

        }).start(port);

        return app;
    }

    public static void stopServer(Javalin app)
    {
        app.stop();
    }

    private static Routes buildRoutes(ApplicationContext applicationContext)
    {
        return new Routes(
                new LanguageRoute(applicationContext.getLanguageController()),
                new TraitRoute(applicationContext.getTraitController()),
                new RaceRoute(applicationContext.getRaceController()),
                new SubraceRoute(applicationContext.getSubraceController())
        );
    }

}