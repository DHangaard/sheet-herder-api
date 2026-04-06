package app.config;

import app.config.hibernate.HibernateConfig;
import app.controllers.implementations.*;
import app.controllers.interfaces.ICharacterSheetController;
import app.controllers.interfaces.IHealthCheckController;
import app.controllers.interfaces.IReferenceController;
import app.controllers.interfaces.IUserController;
import app.dtos.dnd.DNDLanguageDetailDTO;
import app.dtos.dnd.DNDRaceDetailDTO;
import app.dtos.dnd.DNDSubraceDetailDTO;
import app.dtos.dnd.DNDTraitDetailDTO;
import app.dtos.reference.LanguageDTO;
import app.dtos.reference.RaceDTO;
import app.dtos.reference.SubraceDTO;
import app.dtos.reference.TraitDTO;
import app.integrations.DNDClient;
import app.integrations.IDNDClient;
import app.persistence.daos.domain.implementations.CharacterSheetDAO;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.daos.reference.implementations.LanguageDAO;
import app.persistence.daos.reference.implementations.RaceDAO;
import app.persistence.daos.reference.implementations.SubraceDAO;
import app.persistence.daos.reference.implementations.TraitDAO;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
import app.persistence.entities.reference.Trait;
import app.security.controllers.ISecurityController;
import app.security.controllers.SecurityController;
import app.persistence.daos.domain.interfaces.IUserDAO;
import app.persistence.daos.domain.implementations.UserDAO;
import app.security.services.ISecurityService;
import app.security.services.SecurityService;
import app.services.domain.implementations.CharacterSheetService;
import app.services.domain.implementations.UserService;
import app.services.domain.interfaces.ICharacterSheetService;
import app.services.domain.interfaces.IUserService;
import app.services.reference.implementations.*;
import app.services.reference.interfaces.IDNDFetchingService;
import app.services.reference.interfaces.IReferenceDataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityManagerFactory;
import lombok.Getter;

import java.net.http.HttpClient;

public final class DIContainer
{
    private static DIContainer instance;
    private final EntityManagerFactory entityManagerFactory;
    private final HttpClient httpClient;
    @Getter
    private final ObjectMapper objectMapper;

    private final IDNDClient dndClient;
    @Getter
    private final IDNDFetchingService dndFetchingService;

    private final IReferenceDAO<Language> languageDAO;
    private final IReferenceDAO<Trait> traitDAO;
    private final IReferenceDAO<Race> raceDAO;
    private final IReferenceDAO<Subrace> subraceDAO;

    private final IUserDAO userDAO;
    private final ICharacterSheetDAO characterSheetDAO;

    @Getter
    private final IReferenceDataService<DNDLanguageDetailDTO, LanguageDTO> languageService;
    @Getter
    private final IReferenceDataService<DNDTraitDetailDTO, TraitDTO> traitService;
    @Getter
    private final IReferenceDataService<DNDRaceDetailDTO, RaceDTO> raceService;
    @Getter
    private final IReferenceDataService<DNDSubraceDetailDTO, SubraceDTO> subraceService;

    @Getter
    private final ISecurityService securityService;
    @Getter
    private final IUserService userService;
    @Getter
    private final ICharacterSheetService characterSheetService;

    @Getter
    private final IHealthCheckController healthCheckController;

    @Getter
    private final IReferenceController languageController;
    @Getter
    private final IReferenceController traitController;
    @Getter
    private final IReferenceController raceController;
    @Getter
    private final IReferenceController subraceController;

    @Getter
    private final ISecurityController securityController;
    @Getter
    private final IUserController userController;
    @Getter
    private final ICharacterSheetController characterSheetController;


    public DIContainer(EntityManagerFactory entityManagerFactory)
    {
        this.entityManagerFactory = entityManagerFactory;

        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.dndClient = new DNDClient(httpClient, objectMapper);
        this.dndFetchingService = new DNDFetchingService(dndClient);

        this.languageDAO = new LanguageDAO(entityManagerFactory);
        this.traitDAO = new TraitDAO(entityManagerFactory);
        this.raceDAO = new RaceDAO(entityManagerFactory);
        this.subraceDAO = new SubraceDAO(entityManagerFactory);

        this.userDAO = new UserDAO(entityManagerFactory);
        this.characterSheetDAO = new CharacterSheetDAO(entityManagerFactory);

        this.languageService = new LanguageService(languageDAO);
        this.traitService = new TraitService(traitDAO);
        this.raceService = new RaceService(raceDAO, languageDAO, traitDAO);
        this.subraceService = new SubraceService(subraceDAO, raceDAO, traitDAO);

        this.securityService = new SecurityService(userDAO);
        this.userService = new UserService(userDAO);
        this.characterSheetService = new CharacterSheetService(characterSheetDAO, raceDAO, subraceDAO, languageDAO);

        this.healthCheckController = new HealthCheckController(entityManagerFactory);

        this.languageController = new LanguageController(languageService);
        this.traitController = new TraitController(traitService);
        this.raceController = new RaceController(raceService);
        this.subraceController = new SubraceController(subraceService);

        this.securityController = new SecurityController(securityService);
        this.userController = new UserController(userService);
        this.characterSheetController = new CharacterSheetController(characterSheetService, userService);
    }

    public static DIContainer getInstance()
    {
        if (instance == null)
        {
            instance = new DIContainer(HibernateConfig.getEntityManagerFactory());
        }
        return instance;
    }

    public static DIContainer getTestInstance(EntityManagerFactory entityManagerFactory)
    {
        instance = new DIContainer(entityManagerFactory);
        return instance;
    }
}
