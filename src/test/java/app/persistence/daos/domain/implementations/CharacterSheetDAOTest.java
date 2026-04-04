package app.persistence.daos.domain.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CharacterSheetDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private ICharacterSheetDAO characterSheetDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator testPopulator = new TestPopulator(emf);
        testPopulator.populate();
        seeded = testPopulator.getSeededData();
        characterSheetDAO = new CharacterSheetDAO(emf);
    }

    @AfterAll
    void shutdown()
    {
        emf.close();
    }

    @Test
    @DisplayName("Create - Should persist character sheet, normalize name and set timestamps")
    void create()
    {
        User john = (User) seeded.get("john");
        CharacterSheet newSheet = new CharacterSheet(john, " Merlin ", null, null, Set.of(), Map.of());

        CharacterSheet result = characterSheetDAO.create(newSheet);

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getUser(), notNullValue());
        assertThat(result.getUser().getId(), equalTo(john.getId()));
        assertThat(result.getName(), equalTo("Merlin"));
        assertThat(result.getCreatedAt(), notNullValue());
        assertThat(result.getUpdatedAt(), notNullValue());
    }

    @Test
    @DisplayName("Create - Should throw exception on duplicate name for same user")
    void createDuplicateName()
    {
        User john = (User) seeded.get("john");
        CharacterSheet duplicate = new CharacterSheet(john, "Aragorn", null, null, Set.of(), Map.of());

        assertThrows(DatabaseException.class, () -> characterSheetDAO.create(duplicate));
    }

    @Test
    @DisplayName("GetById - Should return correct character sheet by id")
    void getById()
    {
        CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");

        CharacterSheet result = characterSheetDAO.getById(johnSheet1.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getId(), equalTo(johnSheet1.getId()));
        assertThat(result.getUser(), notNullValue());
        assertThat(result.getUser().getId(), equalTo(johnSheet1.getUser().getId()));
        assertThat(result.getName(), equalTo(johnSheet1.getName()));
        assertThat(result.getCreatedAt(), notNullValue());
        assertThat(result.getUpdatedAt(), notNullValue());
    }

    @Test
    @DisplayName("GetById - Should throw NotFoundException when id does not exist")
    void getByIdNotFound()
    {
        assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(999L));
    }

    @Test
    @DisplayName("Update - Should persist updated (normalized) name and set new updatedAt timestamp")
    void update()
    {
        CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");
        johnSheet1.setName(" Strider ");
        characterSheetDAO.update(johnSheet1);

        CharacterSheet result = characterSheetDAO.getById(johnSheet1.getId());

        assertThat(result, notNullValue());
        assertThat(result.getId(), notNullValue());
        assertThat(result.getId(), equalTo(johnSheet1.getId()));
        assertThat(result.getUser(), notNullValue());
        assertThat(result.getUser().getId(), equalTo(johnSheet1.getUser().getId()));
        assertThat(result.getName(), equalTo("Strider"));
        assertThat(result.getCreatedAt(), notNullValue());
        assertThat(result.getUpdatedAt(), notNullValue());
        assertThat(result.getUpdatedAt(), not(equalTo(result.getCreatedAt())));
    }

    @Test
    @DisplayName("Update - Should throw exception on duplicate name for same user")
    void updateDuplicateName()
    {
        CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");
        johnSheet1.setName("Legolas");
        assertThrows(DatabaseException.class, () -> characterSheetDAO.update(johnSheet1));
    }

    @Test
    @DisplayName("Delete - Should delete character sheet and throw when fetched afterwards")
    void delete()
    {
        CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");

        Long result = characterSheetDAO.delete(johnSheet1.getId());

        assertThat(result, notNullValue());
        assertThat(result, equalTo(johnSheet1.getId()));
        assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(result));
    }

    @Test
    @DisplayName("Delete - Should throw NotFoundException when id does not exist")
    void deleteNotFound()
    {
        assertThrows(NotFoundException.class, () -> characterSheetDAO.delete(999L));
    }

    @Test
    @DisplayName("FindAllByUser - Should return only sheets belonging to the given user")
    void findAllByUser()
    {
        User john = (User) seeded.get("john");
        User morten = (User) seeded.get("morten");
        CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");
        CharacterSheet johnSheet2 = (CharacterSheet) seeded.get("johnSheet2");
        CharacterSheet mortenSheet1 = (CharacterSheet) seeded.get("mortenSheet1");

        List<CharacterSheet> johnResults = characterSheetDAO.findAllByUser(john);
        List<CharacterSheet> mortenResults = characterSheetDAO.findAllByUser(morten);

        assertThat(johnResults, notNullValue());
        assertThat(johnResults, hasSize(2));
        assertThat(johnResults, containsInAnyOrder(johnSheet1, johnSheet2));
        assertThat(johnResults, not(hasItem(mortenSheet1)));

        assertThat(mortenResults, notNullValue());
        assertThat(mortenResults, hasSize(1));
        assertThat(mortenResults, containsInAnyOrder(mortenSheet1));
        assertThat(mortenResults, not(hasItems(johnSheet1, johnSheet2)));
    }

    @Test
    @DisplayName("FindAllByUser - Should return empty list when user has no character sheets")
    void findAllByUserEmpty()
    {
        User morten = (User) seeded.get("morten");
        characterSheetDAO.delete(((CharacterSheet) seeded.get("mortenSheet1")).getId());

        List<CharacterSheet> results = characterSheetDAO.findAllByUser(morten);

        assertThat(results, notNullValue());
        assertThat(results, hasSize(0));
    }
}