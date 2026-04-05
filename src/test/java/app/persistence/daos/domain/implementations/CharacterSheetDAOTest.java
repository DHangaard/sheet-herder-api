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
    void tearDown()
    {
        emf.close();
    }

    @Nested
    @DisplayName("Create")
    class Create
    {
        @Test
        @DisplayName("Should persist character sheet, normalize name and set timestamps")
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
        @DisplayName("Should throw DatabaseException on duplicate name for same user")
        void createDuplicateName()
        {
            User john = (User) seeded.get("john");
            CharacterSheet duplicate = new CharacterSheet(john, "Aragorn", null, null, Set.of(), Map.of());

            assertThrows(DatabaseException.class, () -> characterSheetDAO.create(duplicate));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct character sheet by id")
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
        @DisplayName("Should throw NotFoundException when id does not exist")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated (normalized) name and set new updatedAt timestamp")
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
        @DisplayName("Should throw DatabaseException on duplicate name for same user")
        void updateDuplicateName()
        {
            CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");

            johnSheet1.setName("Legolas");

            assertThrows(DatabaseException.class, () -> characterSheetDAO.update(johnSheet1));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete character sheet and throw NotFoundException when fetched afterwards")
        void delete()
        {
            CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");

            Long result = characterSheetDAO.delete(johnSheet1.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(johnSheet1.getId()));
            assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException when id does not exist")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> characterSheetDAO.delete(999L));
        }
    }

    @Nested
    @DisplayName("GetAllByUser")
    class GetAllByUser
    {
        @Test
        @DisplayName("Should return only sheets belonging to the given user")
        void getAllByUser()
        {
            User john = (User) seeded.get("john");
            User morten = (User) seeded.get("morten");
            CharacterSheet johnSheet1 = (CharacterSheet) seeded.get("johnSheet1");
            CharacterSheet johnSheet2 = (CharacterSheet) seeded.get("johnSheet2");
            CharacterSheet mortenSheet1 = (CharacterSheet) seeded.get("mortenSheet1");

            List<CharacterSheet> johnResults = characterSheetDAO.getAllByUser(john);
            List<CharacterSheet> mortenResults = characterSheetDAO.getAllByUser(morten);

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
        @DisplayName("Should return empty list when user has no character sheets")
        void getAllByUserEmpty()
        {
            User gary = (User) seeded.get("gary");
            User morten = (User) seeded.get("morten");
            characterSheetDAO.delete(((CharacterSheet) seeded.get("mortenSheet1")).getId());

            List<CharacterSheet> garyResults = characterSheetDAO.getAllByUser(gary);
            List<CharacterSheet> mortenResults = characterSheetDAO.getAllByUser(morten);

            assertThat(garyResults, notNullValue());
            assertThat(garyResults, hasSize(0));

            assertThat(mortenResults, notNullValue());
            assertThat(mortenResults, hasSize(0));
        }
    }
}