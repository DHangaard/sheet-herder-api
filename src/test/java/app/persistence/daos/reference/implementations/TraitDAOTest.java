package app.persistence.daos.reference.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.reference.Trait;
import app.testutils.ReferenceDataPopulator;
import app.testutils.TestCleanDB;
import app.utils.ContentHashing;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraitDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IReferenceDAO<Trait> traitDAO;
    private Map<String, IEntity> seeded;

    private static final int TOTAL_NUM_TRAITS = 5;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        ReferenceDataPopulator populator = new ReferenceDataPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        traitDAO = new TraitDAO(emf);
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
        @DisplayName("Should persist trait and return it with id and timestamps")
        void create()
        {
            Trait newTrait = new Trait(
                    "Lucky",
                    List.of("When you roll a 1 on the d20 for an attack roll, ability check, or saving throw, you can reroll the die and must use the new roll."),
                    ContentHashing.sha256Hex("Lucky")
            );

            Trait result = traitDAO.create(newTrait);

            assertThat(result, notNullValue());
            assertThat(result.getId(), notNullValue());
            assertThat(result.getName(), equalTo("Lucky"));
            assertThat(result.getDescriptions(), hasSize(1));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void createDuplicateName()
        {
            Trait duplicate = new Trait(
                    "Darkvision",
                    List.of("Some other description."),
                    ContentHashing.sha256Hex("Darkvision-duplicate")
            );

            assertThrows(DatabaseException.class, () -> traitDAO.create(duplicate));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void createNullName()
        {
            Trait nullName = new Trait(
                    null,
                    List.of("Some description."),
                    ContentHashing.sha256Hex("null-name")
            );

            assertThrows(DatabaseException.class, () -> traitDAO.create(nullName));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void createNullContentHash()
        {
            Trait nullHash = new Trait(
                    "Brave",
                    List.of("You have advantage on saving throws against being frightened."),
                    null
            );

            assertThrows(DatabaseException.class, () -> traitDAO.create(nullHash));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct trait by id")
        void getById()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");

            Trait result = traitDAO.getById(darkvision.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(darkvision.getId()));
            assertThat(result.getName(), equalTo("Darkvision"));
            assertThat(result.getDescriptions(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> traitDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated descriptions and return updated trait")
        void update()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");
            darkvision.setDescriptions(List.of("Updated description."));

            Trait result = traitDAO.update(darkvision);
            Trait fetched = traitDAO.getById(darkvision.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(darkvision.getId()));
            assertThat(fetched.getDescriptions(), contains("Updated description."));
            assertThat(fetched.getContentHash(), notNullValue());
            assertThat(fetched.getCreatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), not(equalTo(result.getCreatedAt())));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void updateDuplicateName()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");
            darkvision.setName("Keen Senses");

            assertThrows(DatabaseException.class, () -> traitDAO.update(darkvision));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void updateNullName()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");
            darkvision.setName(null);

            assertThrows(DatabaseException.class, () -> traitDAO.update(darkvision));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void updateNullContentHash()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");
            darkvision.setContentHash(null);

            assertThrows(DatabaseException.class, () -> traitDAO.update(darkvision));
        }

        @Test
        @DisplayName("Should throw DatabaseException when id does not exist")
        void updateNotFound()
        {
            Trait brave = (Trait) seeded.get("brave");
            traitDAO.delete(brave.getId());

            assertThrows(DatabaseException.class, () -> traitDAO.update(brave));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete trait and throw NotFoundException when fetched afterwards")
        void delete()
        {
            Trait brave = (Trait) seeded.get("brave");

            Long result = traitDAO.delete(brave.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(brave.getId()));
            assertThrows(NotFoundException.class, () -> traitDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> traitDAO.delete(999L));
        }

        @Test
        @DisplayName("Should throw DatabaseException when trait is still referenced by a race")
        void deleteReferencedByRace()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");

            assertThrows(DatabaseException.class, () -> traitDAO.delete(darkvision.getId()));
        }
    }

    @Nested
    @DisplayName("GetByName")
    class GetByName
    {
        @Test
        @DisplayName("Should return correct trait by name")
        void getByName()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");

            Trait result = traitDAO.getByName("Darkvision");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(darkvision.getId()));
            assertThat(result.getName(), equalTo("Darkvision"));
            assertThat(result.getDescriptions(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should return correct trait regardless of input casing")
        void getByNameCaseInsensitive()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");

            Trait result = traitDAO.getByName("dArKvIsIoN");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(darkvision.getId()));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown name")
        void getByNameNotFound()
        {
            assertThrows(NotFoundException.class, () -> traitDAO.getByName("NonExistentTrait"));
        }
    }

    @Nested
    @DisplayName("GetByNames")
    class GetByNames
    {
        @Test
        @DisplayName("Should return all traits matching the given names")
        void getByNames()
        {
            List<Trait> result = traitDAO.getByNames(Set.of("Darkvision", "Keen Senses"));

            assertThat(result, notNullValue());
            assertThat(result, hasSize(2));
            assertThat(result.stream()
                    .map(Trait::getName)
                    .toList(), containsInAnyOrder("Darkvision", "Keen Senses"));
        }

        @Test
        @DisplayName("Should return results regardless of input casing")
        void getByNamesCaseInsensitive()
        {
            List<Trait> result = traitDAO.getByNames(Set.of("dArKvIsIoN", "KEEN SENSES"));

            assertThat(result, hasSize(2));
        }

        @Test
        @DisplayName("Should return empty list when no names match")
        void getByNamesNoMatch()
        {
            List<Trait> result = traitDAO.getByNames(Set.of("NonExistent"));

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void getByNamesEmpty()
        {
            List<Trait> result = traitDAO.getByNames(Set.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("GetAll")
    class GetAll
    {
        @Test
        @DisplayName("Should return all traits")
        void getAll()
        {
            List<Trait> result = traitDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, hasSize(TOTAL_NUM_TRAITS));
            assertThat(result.stream()
                    .map(Trait::getName)
                    .toList(), containsInAnyOrder("Darkvision", "Keen Senses", "Fey Ancestry", "Trance", "Brave"));
        }

        @Test
        @DisplayName("Should return empty list when no traits exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            List<Trait> result = traitDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("SyncAll")
    class SyncAll
    {
        @Test
        @DisplayName("Should insert new trait when name does not exist")
        void syncAllInsert()
        {
            Trait newTrait = new Trait(
                    "Lucky",
                    List.of("When you roll a 1 on the d20 for an attack roll, ability check, or saving throw, you can reroll the die and must use the new roll."),
                    ContentHashing.sha256Hex("Lucky")
            );

            List<Trait> result = traitDAO.syncAll(List.of(newTrait));
            List<Trait> all = traitDAO.getAll();

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), notNullValue());
            assertThat(result.get(0).getName(), equalTo("Lucky"));
            assertThat(all, hasSize(TOTAL_NUM_TRAITS + result.size()));
        }

        @Test
        @DisplayName("Should return existing trait unchanged when contentHash matches")
        void syncAllSkip()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");

            List<Trait> result = traitDAO.syncAll(List.of(darkvision));
            List<Trait> all = traitDAO.getAll();
            Trait fetched = traitDAO.getById(darkvision.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(darkvision.getId()));
            assertThat(all, hasSize(TOTAL_NUM_TRAITS));
            assertThat(fetched.getContentHash(), equalTo(darkvision.getContentHash()));
        }

        @Test
        @DisplayName("Should update existing trait when contentHash differs")
        void syncAllUpdate()
        {
            Trait darkvision = (Trait) seeded.get("darkvision");
            Trait incoming = new Trait(
                    "Darkvision",
                    List.of("Updated description."),
                    ContentHashing.sha256Hex("Darkvision-updated")
            );

            List<Trait> result = traitDAO.syncAll(List.of(incoming));
            List<Trait> all = traitDAO.getAll();
            Trait fetched = traitDAO.getById(darkvision.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(darkvision.getId()));
            assertThat(all, hasSize(TOTAL_NUM_TRAITS));
            assertThat(fetched.getDescriptions(), contains("Updated description."));
        }

        @Test
        @DisplayName("Should deduplicate incoming list — only one entry per name is persisted")
        void syncAllDeduplication()
        {
            Trait first = new Trait(
                    "Lucky",
                    List.of("First description."),
                    ContentHashing.sha256Hex("Lucky-first")
            );
            Trait second = new Trait(
                    "Lucky",
                    List.of("Second description."),
                    ContentHashing.sha256Hex("Lucky-second")
            );

            traitDAO.syncAll(List.of(first, second));
            List<Trait> all = traitDAO.getAll();

            assertThat(all.stream()
                    .filter(trait -> trait.getName().equals("Lucky"))
                    .toList(), hasSize(1));
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void syncAllEmpty()
        {
            List<Trait> result = traitDAO.syncAll(List.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }
}