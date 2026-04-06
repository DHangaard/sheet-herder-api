package app.routes;

import app.security.routes.SecurityRoute;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.path;

public class Routes
{
    private final HealthCheckRoute healthCheckRoute;
    private final LanguageRoute languageRoute;
    private final TraitRoute traitRoute;
    private final RaceRoute raceRoute;
    private final SubraceRoute subraceRoute;
    private final SecurityRoute securityRoute;
    private final UserRoute userRoute;
    private final CharacterSheetRoute characterSheetRoute;

    public Routes(
            HealthCheckRoute healthCheckRoute,
            LanguageRoute languageRoute,
            TraitRoute traitRoute,
            RaceRoute raceRoute,
            SubraceRoute subraceRoute,
            SecurityRoute securityRoute,
            UserRoute userRoute,
            CharacterSheetRoute characterSheetRoute)
    {
        this.healthCheckRoute = healthCheckRoute;
        this.languageRoute = languageRoute;
        this.traitRoute = traitRoute;
        this.raceRoute = raceRoute;
        this.subraceRoute = subraceRoute;
        this.securityRoute = securityRoute;
        this.userRoute = userRoute;
        this.characterSheetRoute = characterSheetRoute;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("/api/v1", () ->
            {
                healthCheckRoute.getRoutes().addEndpoints();
                languageRoute.getRoutes().addEndpoints();
                traitRoute.getRoutes().addEndpoints();
                raceRoute.getRoutes().addEndpoints();
                subraceRoute.getRoutes().addEndpoints();
                securityRoute.getRoutes().addEndpoints();
                userRoute.getRoutes().addEndpoints();
                characterSheetRoute.getRoutes().addEndpoints();
            });
        };
    }
}
