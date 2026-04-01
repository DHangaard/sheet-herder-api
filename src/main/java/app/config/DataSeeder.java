package app.config;

import app.dtos.dnd.DNDLanguageDetailDTO;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.dtos.dnd.DNDSubraceDetailDTO;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.utils.ExecutionTimer;

import java.util.List;

public class DataSeeder
{
    private DataSeeder()
    {
    }

    public static void seed(DIContainer diContainer)
    {
        List<DNDLanguageDetailDTO> languages = diContainer.getDndFetchingService().fetchAllLanguagesWithDetails();
        List<DNDTraitDetailDTO> traits = diContainer.getDndFetchingService().fetchAllTraitsWithDetails();
        List<DNDRaceDetailDTO> races = diContainer.getDndFetchingService().fetchAllRacesWithDetails();
        List<DNDSubraceDetailDTO> subraces = diContainer.getDndFetchingService().fetchAllSubracesWithDetails();

        ExecutionTimer.split("Reference data fetched");

        diContainer.getLanguageService().persistAll(languages);
        diContainer.getTraitService().persistAll(traits);
        diContainer.getRaceService().persistAll(races);
        diContainer.getSubraceService().persistAll(subraces);

        ExecutionTimer.split("Reference data synchronized");
    }
}
