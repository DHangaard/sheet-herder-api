package app.persistence.daos.reference.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.enums.Ability;
import app.enums.Size;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
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
class RaceDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IReferenceDAO<Race> raceDAO;
    private Map<String, IEntity> seeded;

    private static final int TOTAL_NUM_RACES = 2;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        ReferenceDataPopulator populator = new ReferenceDataPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        raceDAO = new RaceDAO(emf);
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
        @DisplayName("Should persist race and return it with id, associations and timestamps")
        void create()
        {
            Language common = (Language) seeded.get("common");
            Trait brave = (Trait) seeded.get("brave");

            Race newRace = new Race(
                    "Halfling",
                    25,
                    Map.of(Ability.DEXTERITY, 2),
                    "A halfling reaches adulthood at the age of 20.",
                    "Most halflings are lawful good.",
                    Size.SMALL,
                    "Halflings average about 3 feet tall. Your size is Small.",
                    Set.of(common),
                    "You can speak, read, and write Common.",
                    Set.of(brave),
                    ContentHashing.sha256Hex("Halfling")
            );

            Race result = raceDAO.create(newRace);

            assertThat(result, notNullValue());
            assertThat(result.getId(), notNullValue());
            assertThat(result.getName(), equalTo("Halfling"));
            assertThat(result.getSpeed(), equalTo(25));
            assertThat(result.getSize(), equalTo(Size.SMALL));
            assertThat(result.getAbilityBonuses(), hasEntry(Ability.DEXTERITY, 2));
            assertThat(result.getLanguages(), hasSize(1));
            assertThat(result.getTraits(), hasSize(1));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void createDuplicateName()
        {
            Language common = (Language) seeded.get("common");

            Race duplicate = new Race(
                    "Elf",
                    30,
                    Map.of(Ability.DEXTERITY, 2),
                    "Some age description.",
                    "Some alignment.",
                    Size.MEDIUM,
                    "Some size description.",
                    Set.of(common),
                    "Some language description.",
                    Set.of(),
                    ContentHashing.sha256Hex("Elf-duplicate")
            );

            assertThrows(DatabaseException.class, () -> raceDAO.create(duplicate));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void createNullName()
        {
            Race nullName = new Race(
                    null,
                    30,
                    Map.of(),
                    "Some age description.",
                    "Some alignment.",
                    Size.MEDIUM,
                    "Some size description.",
                    Set.of(),
                    "Some language description.",
                    Set.of(),
                    ContentHashing.sha256Hex("null-name")
            );

            assertThrows(DatabaseException.class, () -> raceDAO.create(nullName));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void createNullContentHash()
        {
            Race nullHash = new Race(
                    "Halfling",
                    25,
                    Map.of(),
                    "Some age description.",
                    "Some alignment.",
                    Size.SMALL,
                    "Some size description.",
                    Set.of(),
                    "Some language description.",
                    Set.of(),
                    null
            );

            assertThrows(DatabaseException.class, () -> raceDAO.create(nullHash));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct race by id with associations populated")
        void getById()
        {
            Race elf = (Race) seeded.get("elf");

            Race result = raceDAO.getById(elf.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elf.getId()));
            assertThat(result.getName(), equalTo("Elf"));
            assertThat(result.getSpeed(), equalTo(30));
            assertThat(result.getSize(), equalTo(Size.MEDIUM));
            assertThat(result.getAbilityBonuses(), hasEntry(Ability.DEXTERITY, 2));
            assertThat(result.getLanguages(), not(empty()));
            assertThat(result.getTraits(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> raceDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated speed and return updated race")
        void update()
        {
            Race human = (Race) seeded.get("human");
            human.setSpeed(25);

            Race result = raceDAO.update(human);
            Race fetched = raceDAO.getById(human.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(human.getId()));
            assertThat(fetched.getSpeed(), equalTo(25));
            assertThat(fetched.getLanguages(), not(empty()));
            assertThat(fetched.getTraits(), empty());
            assertThat(fetched.getContentHash(), notNullValue());
            assertThat(fetched.getCreatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), not(equalTo(fetched.getCreatedAt())));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void updateDuplicateName()
        {
            Race human = (Race) seeded.get("human");
            human.setName("Elf");

            assertThrows(DatabaseException.class, () -> raceDAO.update(human));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void updateNullName()
        {
            Race human = (Race) seeded.get("human");
            human.setName(null);

            assertThrows(DatabaseException.class, () -> raceDAO.update(human));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void updateNullContentHash()
        {
            Race human = (Race) seeded.get("human");
            human.setContentHash(null);

            assertThrows(DatabaseException.class, () -> raceDAO.update(human));
        }

        @Test
        @DisplayName("Should throw DatabaseException when id does not exist")
        void updateNotFound()
        {
            Race human = (Race) seeded.get("human");
            raceDAO.delete(human.getId());

            assertThrows(DatabaseException.class, () -> raceDAO.update(human));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete race and throw NotFoundException when fetched afterwards")
        void delete()
        {
            Race human = (Race) seeded.get("human");

            Long result = raceDAO.delete(human.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(human.getId()));
            assertThrows(NotFoundException.class, () -> raceDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> raceDAO.delete(999L));
        }

        @Test
        @DisplayName("Should throw DatabaseException when race is still referenced by a subrace")
        void deleteReferencedBySubrace()
        {
            Race elf = (Race) seeded.get("elf");

            assertThrows(DatabaseException.class, () -> raceDAO.delete(elf.getId()));
        }
    }

    @Nested
    @DisplayName("GetByName")
    class GetByName
    {
        @Test
        @DisplayName("Should return correct race by name with associations populated")
        void getByName()
        {
            Race elf = (Race) seeded.get("elf");

            Race result = raceDAO.getByName("Elf");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elf.getId()));
            assertThat(result.getName(), equalTo("Elf"));
            assertThat(result.getLanguages(), not(empty()));
            assertThat(result.getTraits(), not(empty()));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should return correct race regardless of input casing")
        void getByNameCaseInsensitive()
        {
            Race elf = (Race) seeded.get("elf");

            Race result = raceDAO.getByName("eLf");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elf.getId()));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown name")
        void getByNameNotFound()
        {
            assertThrows(NotFoundException.class, () -> raceDAO.getByName("NonExistentRace"));
        }
    }

    @Nested
    @DisplayName("GetByNames")
    class GetByNames
    {
        @Test
        @DisplayName("Should return all races matching the given names")
        void getByNames()
        {
            List<Race> result = raceDAO.getByNames(Set.of("Elf", "Human"));

            assertThat(result, notNullValue());
            assertThat(result, hasSize(2));
            assertThat(result.stream()
                    .map(Race::getName)
                    .toList(), containsInAnyOrder("Elf", "Human"));
        }

        @Test
        @DisplayName("Should return results regardless of input casing")
        void getByNamesCaseInsensitive()
        {
            List<Race> result = raceDAO.getByNames(Set.of("eLf", "HUMAN"));

            assertThat(result, hasSize(2));
        }

        @Test
        @DisplayName("Should return empty list when no names match")
        void getByNamesNoMatch()
        {
            List<Race> result = raceDAO.getByNames(Set.of("NonExistent"));

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void getByNamesEmpty()
        {
            List<Race> result = raceDAO.getByNames(Set.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("GetAll")
    class GetAll
    {
        @Test
        @DisplayName("Should return all seeded races")
        void getAll()
        {
            List<Race> result = raceDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, hasSize(TOTAL_NUM_RACES));
            assertThat(result.stream()
                    .map(Race::getName)
                    .toList(), containsInAnyOrder("Elf", "Human"));
            assertThat(result.stream().allMatch(r -> r.getLanguages() != null), is(true));
            assertThat(result.stream().allMatch(r -> r.getTraits() != null), is(true));
        }

        @Test
        @DisplayName("Should return empty list when no races exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            List<Race> result = raceDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("SyncAll")
    class SyncAll
    {
        @Test
        @DisplayName("Should insert new race when name does not exist")
        void syncAllInsert()
        {
            Language common = (Language) seeded.get("common");
            Trait brave = (Trait) seeded.get("brave");

            Race newRace = new Race(
                    "Halfling",
                    25,
                    Map.of(Ability.DEXTERITY, 2),
                    "A halfling reaches adulthood at the age of 20.",
                    "Most halflings are lawful good.",
                    Size.SMALL,
                    "Halflings average about 3 feet tall. Your size is Small.",
                    Set.of(common),
                    "You can speak, read, and write Common.",
                    Set.of(brave),
                    ContentHashing.sha256Hex("Halfling")
            );

            List<Race> result = raceDAO.syncAll(List.of(newRace));
            List<Race> all = raceDAO.getAll();

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), notNullValue());
            assertThat(result.get(0).getName(), equalTo("Halfling"));
            assertThat(all, hasSize(TOTAL_NUM_RACES + result.size()));
        }

        @Test
        @DisplayName("Should return existing race unchanged when contentHash matches")
        void syncAllSkip()
        {
            Race elf = (Race) seeded.get("elf");

            List<Race> result = raceDAO.syncAll(List.of(elf));
            List<Race> all = raceDAO.getAll();
            Race fetched = raceDAO.getById(elf.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(elf.getId()));
            assertThat(all, hasSize(TOTAL_NUM_RACES));
            assertThat(fetched.getContentHash(), equalTo(elf.getContentHash()));
        }

        @Test
        @DisplayName("Should update existing race when contentHash differs")
        void syncAllUpdate()
        {
            Race elf = (Race) seeded.get("elf");
            Language elvish = (Language) seeded.get("elvish");
            Language common = (Language) seeded.get("common");
            Trait darkvision = (Trait) seeded.get("darkvision");

            Race incoming = new Race(
                    "Elf",
                    35,
                    Map.of(Ability.DEXTERITY, 2),
                    "Updated age description.",
                    "Updated alignment.",
                    Size.MEDIUM,
                    "Updated size description.",
                    Set.of(elvish, common),
                    "Updated language description.",
                    Set.of(darkvision),
                    ContentHashing.sha256Hex("Elf-updated")
            );

            List<Race> result = raceDAO.syncAll(List.of(incoming));
            List<Race> all = raceDAO.getAll();
            Race fetched = raceDAO.getById(elf.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(elf.getId()));
            assertThat(all, hasSize(TOTAL_NUM_RACES));
            assertThat(fetched.getSpeed(), equalTo(35));
            assertThat(fetched.getAgeDescription(), equalTo("Updated age description."));
        }

        @Test
        @DisplayName("Should deduplicate incoming list — only one entry per name is persisted")
        void syncAllDeduplication()
        {
            Language common = (Language) seeded.get("common");

            Race first = new Race(
                    "Halfling",
                    25,
                    Map.of(),
                    "First description.",
                    "Some alignment.",
                    Size.SMALL,
                    "Some size description.",
                    Set.of(common),
                    "Some language description.",
                    Set.of(),
                    ContentHashing.sha256Hex("Halfling-first")
            );
            Race second = new Race(
                    "Halfling",
                    30,
                    Map.of(),
                    "Second description.",
                    "Some alignment.",
                    Size.SMALL,
                    "Some size description.",
                    Set.of(common),
                    "Some language description.",
                    Set.of(),
                    ContentHashing.sha256Hex("Halfling-second")
            );

            raceDAO.syncAll(List.of(first, second));
            List<Race> all = raceDAO.getAll();

            assertThat(all.stream()
                    .filter(race -> race.getName().equals("Halfling"))
                    .toList(), hasSize(1));
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void syncAllEmpty()
        {
            List<Race> result = raceDAO.syncAll(List.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }
}