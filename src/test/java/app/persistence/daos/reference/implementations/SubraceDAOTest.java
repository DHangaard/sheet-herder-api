package app.persistence.daos.reference.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.enums.Ability;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
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
class SubraceDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IReferenceDAO<Subrace> subraceDAO;
    private Map<String, IEntity> seeded;

    private static final int TOTAL_NUM_SUBRACES = 2;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        ReferenceDataPopulator populator = new ReferenceDataPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        subraceDAO = new SubraceDAO(emf);
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
        @DisplayName("Should persist subrace and return it with id, race, traits and timestamps")
        void create()
        {
            Race elf = (Race) seeded.get("elf");
            Trait brave = (Trait) seeded.get("brave");

            Subrace newSubrace = new Subrace(
                    "Dark Elf",
                    "Descended from an earlier subrace of dark-skinned elves.",
                    elf,
                    Map.of(Ability.CHARISMA, 1),
                    Set.of(brave),
                    ContentHashing.sha256Hex("Dark Elf")
            );

            Subrace result = subraceDAO.create(newSubrace);

            assertThat(result, notNullValue());
            assertThat(result.getId(), notNullValue());
            assertThat(result.getName(), equalTo("Dark Elf"));
            assertThat(result.getRace(), notNullValue());
            assertThat(result.getRace().getName(), equalTo("Elf"));
            assertThat(result.getAbilityBonuses(), hasEntry(Ability.CHARISMA, 1));
            assertThat(result.getTraits(), hasSize(1));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void createDuplicateName()
        {
            Race elf = (Race) seeded.get("elf");

            Subrace duplicate = new Subrace(
                    "High Elf",
                    "Some other description.",
                    elf,
                    Map.of(),
                    Set.of(),
                    ContentHashing.sha256Hex("High Elf-duplicate")
            );

            assertThrows(DatabaseException.class, () -> subraceDAO.create(duplicate));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void createNullName()
        {
            Race elf = (Race) seeded.get("elf");

            Subrace nullName = new Subrace(
                    null,
                    "Some description.",
                    elf,
                    Map.of(),
                    Set.of(),
                    ContentHashing.sha256Hex("null-name")
            );

            assertThrows(DatabaseException.class, () -> subraceDAO.create(nullName));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void createNullContentHash()
        {
            Race elf = (Race) seeded.get("elf");

            Subrace nullHash = new Subrace(
                    "Dark Elf",
                    "Some description.",
                    elf,
                    Map.of(),
                    Set.of(),
                    null
            );

            assertThrows(DatabaseException.class, () -> subraceDAO.create(nullHash));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct subrace by id with race and traits populated")
        void getById()
        {
            Subrace highElf = (Subrace) seeded.get("highElf");

            Subrace result = subraceDAO.getById(highElf.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(highElf.getId()));
            assertThat(result.getName(), equalTo("High Elf"));
            assertThat(result.getRace(), notNullValue());
            assertThat(result.getRace().getName(), equalTo("Elf"));
            assertThat(result.getTraits(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> subraceDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated description and return updated subrace")
        void update()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");
            woodElf.setDescription("Updated description.");

            Subrace result = subraceDAO.update(woodElf);
            Subrace fetched = subraceDAO.getById(woodElf.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(woodElf.getId()));
            assertThat(fetched.getDescription(), equalTo("Updated description."));
            assertThat(fetched.getRace(), notNullValue());
            assertThat(fetched.getRace().getName(), equalTo("Elf"));
            assertThat(fetched.getContentHash(), notNullValue());
            assertThat(fetched.getCreatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), not(equalTo(fetched.getCreatedAt())));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void updateDuplicateName()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");
            woodElf.setName("High Elf");

            assertThrows(DatabaseException.class, () -> subraceDAO.update(woodElf));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void updateNullName()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");
            woodElf.setName(null);

            assertThrows(DatabaseException.class, () -> subraceDAO.update(woodElf));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void updateNullContentHash()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");
            woodElf.setContentHash(null);

            assertThrows(DatabaseException.class, () -> subraceDAO.update(woodElf));
        }

        @Test
        @DisplayName("Should throw DatabaseException when id does not exist")
        void updateNotFound()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");
            subraceDAO.delete(woodElf.getId());

            assertThrows(DatabaseException.class, () -> subraceDAO.update(woodElf));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete subrace and throw NotFoundException when fetched afterwards")
        void delete()
        {
            Subrace woodElf = (Subrace) seeded.get("woodElf");

            Long result = subraceDAO.delete(woodElf.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(woodElf.getId()));
            assertThrows(NotFoundException.class, () -> subraceDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> subraceDAO.delete(999L));
        }
    }

    @Nested
    @DisplayName("GetByName")
    class GetByName
    {
        @Test
        @DisplayName("Should return correct subrace by name with race and traits populated")
        void getByName()
        {
            Subrace highElf = (Subrace) seeded.get("highElf");

            Subrace result = subraceDAO.getByName("High Elf");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(highElf.getId()));
            assertThat(result.getName(), equalTo("High Elf"));
            assertThat(result.getRace(), notNullValue());
            assertThat(result.getRace().getName(), equalTo("Elf"));
            assertThat(result.getTraits(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should return correct subrace regardless of input casing")
        void getByNameCaseInsensitive()
        {
            Subrace highElf = (Subrace) seeded.get("highElf");

            Subrace result = subraceDAO.getByName("hIgH eLf");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(highElf.getId()));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown name")
        void getByNameNotFound()
        {
            assertThrows(NotFoundException.class, () -> subraceDAO.getByName("NonExistentSubrace"));
        }
    }

    @Nested
    @DisplayName("GetByNames")
    class GetByNames
    {
        @Test
        @DisplayName("Should return all subraces matching the given names")
        void getByNames()
        {
            List<Subrace> result = subraceDAO.getByNames(Set.of("High Elf", "Wood Elf"));

            assertThat(result, notNullValue());
            assertThat(result, hasSize(2));
            assertThat(result.stream()
                    .map(Subrace::getName)
                    .toList(), containsInAnyOrder("High Elf", "Wood Elf"));
        }

        @Test
        @DisplayName("Should return results regardless of input casing")
        void getByNamesCaseInsensitive()
        {
            List<Subrace> result = subraceDAO.getByNames(Set.of("hIgH eLf", "WOOD ELF"));

            assertThat(result, hasSize(2));
        }

        @Test
        @DisplayName("Should return empty list when no names match")
        void getByNamesNoMatch()
        {
            List<Subrace> result = subraceDAO.getByNames(Set.of("NonExistent"));

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void getByNamesEmpty()
        {
            List<Subrace> result = subraceDAO.getByNames(Set.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("GetAll")
    class GetAll
    {
        @Test
        @DisplayName("Should return all subraces")
        void getAll()
        {
            List<Subrace> result = subraceDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, hasSize(TOTAL_NUM_SUBRACES));
            assertThat(result.stream()
                    .map(Subrace::getName)
                    .toList(), containsInAnyOrder("High Elf", "Wood Elf"));
            assertThat(result.stream().allMatch(s -> s.getRace() != null), is(true));
        }

        @Test
        @DisplayName("Should return empty list when no subraces exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            List<Subrace> result = subraceDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("SyncAll")
    class SyncAll
    {
        @Test
        @DisplayName("Should insert new subrace when name does not exist")
        void syncAllInsert()
        {
            Race elf = (Race) seeded.get("elf");
            Trait brave = (Trait) seeded.get("brave");

            Subrace newSubrace = new Subrace(
                    "Dark Elf",
                    "Descended from an earlier subrace of dark-skinned elves.",
                    elf,
                    Map.of(Ability.CHARISMA, 1),
                    Set.of(brave),
                    ContentHashing.sha256Hex("Dark Elf")
            );

            List<Subrace> result = subraceDAO.syncAll(List.of(newSubrace));
            List<Subrace> all = subraceDAO.getAll();

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), notNullValue());
            assertThat(result.get(0).getName(), equalTo("Dark Elf"));
            assertThat(all, hasSize(TOTAL_NUM_SUBRACES + result.size()));
        }

        @Test
        @DisplayName("Should return existing subrace unchanged when contentHash matches")
        void syncAllSkip()
        {
            Subrace highElf = (Subrace) seeded.get("highElf");

            List<Subrace> result = subraceDAO.syncAll(List.of(highElf));
            List<Subrace> all = subraceDAO.getAll();
            Subrace fetched = subraceDAO.getById(highElf.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(highElf.getId()));
            assertThat(all, hasSize(TOTAL_NUM_SUBRACES));
            assertThat(fetched.getContentHash(), equalTo(highElf.getContentHash()));
        }

        @Test
        @DisplayName("Should update existing subrace when contentHash differs")
        void syncAllUpdate()
        {
            Subrace highElf = (Subrace) seeded.get("highElf");
            Race elf = (Race) seeded.get("elf");

            Subrace incoming = new Subrace(
                    "High Elf",
                    "Updated description.",
                    elf,
                    Map.of(Ability.INTELLIGENCE, 1),
                    Set.of(),
                    ContentHashing.sha256Hex("High Elf-updated")
            );

            List<Subrace> result = subraceDAO.syncAll(List.of(incoming));
            List<Subrace> all = subraceDAO.getAll();
            Subrace fetched = subraceDAO.getById(highElf.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(highElf.getId()));
            assertThat(all, hasSize(TOTAL_NUM_SUBRACES));
            assertThat(fetched.getDescription(), equalTo("Updated description."));
        }

        @Test
        @DisplayName("Should deduplicate incoming list — only one entry per name is persisted")
        void syncAllDeduplication()
        {
            Race elf = (Race) seeded.get("elf");

            Subrace first = new Subrace(
                    "Dark Elf",
                    "First description.",
                    elf,
                    Map.of(),
                    Set.of(),
                    ContentHashing.sha256Hex("Dark Elf-first")
            );
            Subrace second = new Subrace(
                    "Dark Elf",
                    "Second description.",
                    elf,
                    Map.of(),
                    Set.of(),
                    ContentHashing.sha256Hex("Dark Elf-second")
            );

            subraceDAO.syncAll(List.of(first, second));
            List<Subrace> all = subraceDAO.getAll();

            assertThat(all.stream()
                    .filter(subrace -> subrace.getName().equals("Dark Elf"))
                    .toList(), hasSize(1));
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void syncAllEmpty()
        {
            List<Subrace> result = subraceDAO.syncAll(List.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }
}