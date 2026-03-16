package app;

import app.dtos.LanguageDTO;
import app.dtos.RaceDTO;
import app.dtos.SubraceDTO;
import app.dtos.TraitDTO;
import app.dtos.dnd.DNDLanguageDetailDTO;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.dtos.dnd.DNDSubraceDetailDTO;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.entities.reference.Language;
import app.entities.reference.Race;
import app.entities.reference.Subrace;
import app.entities.reference.Trait;
import app.integrations.DNDClient;
import app.config.HibernateConfig;
import app.integrations.IDNDClient;
import app.persistence.*;
import app.services.reference.*;
import app.utils.ExecutionTimer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;

import java.net.http.HttpClient;
import java.util.List;

public class Main
{
    // Singleton
    private final static EntityManagerFactory ENTITY_MANAGER_FACTORY = HibernateConfig.getEntityManagerFactory();
    private final static HttpClient CLIENT = HttpClient.newHttpClient();
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    public static void main(String[] args)
    {
        ExecutionTimer.start();

        IDNDClient dndClient = new DNDClient(CLIENT, OBJECT_MAPPER);
        IDNDFetchingService dndFetchingService = new DNDFetchingService(dndClient);

        IReferenceDAO<Language> languageDAO = new LanguageDAO(ENTITY_MANAGER_FACTORY);
        IReferenceDAO<Trait> traitDAO = new TraitDAO(ENTITY_MANAGER_FACTORY);
        IReferenceDAO<Race> raceDAO = new RaceDAO(ENTITY_MANAGER_FACTORY);
        IReferenceDAO<Subrace> subraceDAO = new SubraceDAO(ENTITY_MANAGER_FACTORY);

        IReferenceDataService<DNDLanguageDetailDTO, LanguageDTO> languageService = new LanguageService(languageDAO);
        IReferenceDataService<DNDTraitDetailDTO, TraitDTO> traitService = new TraitService(traitDAO);
        IReferenceDataService<DNDRaceDetailDTO, RaceDTO> raceService = new RaceService(raceDAO, languageDAO, traitDAO);
        IReferenceDataService<DNDSubraceDetailDTO, SubraceDTO> subraceService = new SubraceService(subraceDAO, raceDAO, traitDAO);

        List<DNDLanguageDetailDTO> languages = dndFetchingService.fetchAllLanguagesWithDetails();
        List<DNDTraitDetailDTO> traits = dndFetchingService.fetchAllTraitsWithDetails();
        List<DNDRaceDetailDTO> races = dndFetchingService.fetchAllRacesWithDetails();
        List<DNDSubraceDetailDTO> subraces = dndFetchingService.fetchAllSubracesWithDetails();

        System.out.println("Languages count: " + languages.size());    // Should be 16
        System.out.println("Traits count: " + traits.size());       // Should be 38
        System.out.println("Races count: " + races.size());         // Should be 9
        System.out.println("Subraces count: " + subraces.size());   // Should be 4

        /*
        // TODO Remove
        traits.forEach(t -> {
            t.descriptions().forEach(d -> System.out.printf("Trait[%s] lengths -> description=%d%n", t.name(), len(d)));
        });

        races.forEach(r -> System.out.printf(
                "Race[%s] lengths -> ageDescription=%d, alignment=%d, sizeDescription=%d, languageDescription=%d%n",
                r.name(),
                len(r.ageDescription()),
                len(r.alignment()),
                len(r.size()),
                len(r.languageDescription())
        ));

         subraces.forEach(r -> System.out.printf(
                "Subrace[%s] lengths -> description=%d, alignment=%d, sizeDescription=%d, languageDescription=%d%n",
                r.name(),
                len(r.ageDescription()),
                len(r.alignment()),
                len(r.size()),
                len(r.languageDescription())
        ));
         */

        System.out.println("\nPersisting...\n");

        languageService.persistAll(languages);
        traitService.persistAll(traits);
        raceService.persistAll(races);
        subraceService.persistAll(subraces);

        System.out.println("Languages from DB count: " + languageService.getAll().size());    // Should be 16
        System.out.println("Traits from DB count: " + traitService.getAll().size());       // Should be 38
        System.out.println("Races from DB count: " + raceService.getAll().size());         // Should be 9
        System.out.println("Subraces from DB count: " + subraceService.getAll().size());   // Should be 4

        // dndFetchingService.fetchAllRaces().races().forEach(race -> System.out.println(race.name()));
        // dndFetchingService.fetchAllLanguagesWithDetails().forEach(language -> System.out.println(language.typicalSpeakers()));
        // dndFetchingService.fetchAllRacesWithDetails().forEach(race -> System.out.println(race.size()));

        ENTITY_MANAGER_FACTORY.close();

        System.out.println(ExecutionTimer.finish());
    }

    // TODO Remove
    private static int len(String s) {
        return s == null ?0 : s.length();
    }
}