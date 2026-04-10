package app.persistence.daos.reference.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.reference.Language;
import app.testutils.ReferenceDataPopulator;
import app.testutils.TestCleanDB;
import app.utils.ContentHashing;
import app.enums.LanguageType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LanguageDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IReferenceDAO<Language> languageDAO;
    private Map<String, IEntity> seeded;

    private static final int TOTAL_NUM_LANGUAGES = 3;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        ReferenceDataPopulator populator = new ReferenceDataPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
        languageDAO = new LanguageDAO(emf);
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
        @DisplayName("Should persist language and return it with id and timestamps")
        void create()
        {
            Language newLanguage = new Language(
                    "Dwarvish",
                    "Dwarvish is full of hard consonants and guttural sounds.",
                    LanguageType.STANDARD,
                    List.of("Dwarves"),
                    "Dwarvish",
                    ContentHashing.sha256Hex("Dwarvish")
            );

            Language result = languageDAO.create(newLanguage);

            assertThat(result, notNullValue());
            assertThat(result.getId(), notNullValue());
            assertThat(result.getName(), equalTo("Dwarvish"));
            assertThat(result.getType(), equalTo(LanguageType.STANDARD));
            assertThat(result.getTypicalSpeakers(), contains("Dwarves"));
            assertThat(result.getScript(), equalTo("Dwarvish"));
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void createDuplicateName()
        {
            Language duplicate = new Language(
                    "Elvish",
                    "Some other description.",
                    LanguageType.STANDARD,
                    List.of("Elves"),
                    "Elvish",
                    ContentHashing.sha256Hex("Elvish-duplicate")
            );

            assertThrows(DatabaseException.class, () -> languageDAO.create(duplicate));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void createNullName()
        {
            Language nullName = new Language(
                    null,
                    "Some description.",
                    LanguageType.STANDARD,
                    List.of("Humans"),
                    "Common",
                    ContentHashing.sha256Hex("null-name")
            );

            assertThrows(DatabaseException.class, () -> languageDAO.create(nullName));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void createNullContentHash()
        {
            Language nullHash = new Language(
                    "Dwarvish",
                    "Dwarvish is full of hard consonants and guttural sounds.",
                    LanguageType.STANDARD,
                    List.of("Dwarves"),
                    "Dwarvish",
                    null
            );

            assertThrows(DatabaseException.class, () -> languageDAO.create(nullHash));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct language by id")
        void getById()
        {
            Language elvish = (Language) seeded.get("elvish");

            Language result = languageDAO.getById(elvish.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elvish.getId()));
            assertThat(result.getName(), equalTo("Elvish"));
            assertThat(result.getType(), equalTo(LanguageType.STANDARD));
            assertThat(result.getTypicalSpeakers(), not(empty()));
            assertThat(result.getScript(), notNullValue());
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> languageDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated description and return updated language")
        void update()
        {
            Language sylvan = (Language) seeded.get("sylvan");
            sylvan.setDescription("Updated description.");

            Language result = languageDAO.update(sylvan);
            Language fetched = languageDAO.getById(sylvan.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(sylvan.getId()));
            assertThat(fetched.getDescription(), equalTo("Updated description."));
            assertThat(fetched.getContentHash(), notNullValue());
            assertThat(fetched.getCreatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), notNullValue());
            assertThat(fetched.getUpdatedAt(), not(equalTo(fetched.getCreatedAt())));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate name")
        void updateDuplicateName()
        {
            Language sylvan = (Language) seeded.get("sylvan");
            sylvan.setName("Elvish");

            assertThrows(DatabaseException.class, () -> languageDAO.update(sylvan));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null name")
        void updateNullName()
        {
            Language sylvan = (Language) seeded.get("sylvan");
            sylvan.setName(null);

            assertThrows(DatabaseException.class, () -> languageDAO.update(sylvan));
        }

        @Test
        @DisplayName("Should throw DatabaseException on null contentHash")
        void updateNullContentHash()
        {
            Language sylvan = (Language) seeded.get("sylvan");
            sylvan.setContentHash(null);

            assertThrows(DatabaseException.class, () -> languageDAO.update(sylvan));
        }

        @Test
        @DisplayName("Should throw DatabaseException when id does not exist")
        void updateNotFound()
        {
            Language sylvan = (Language) seeded.get("sylvan");
            languageDAO.delete(sylvan.getId());

            assertThrows(DatabaseException.class, () -> languageDAO.update(sylvan));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete language and throw NotFoundException when fetched afterwards")
        void delete()
        {
            Language sylvan = (Language) seeded.get("sylvan");

            Long result = languageDAO.delete(sylvan.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(sylvan.getId()));
            assertThrows(NotFoundException.class, () -> languageDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown id")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> languageDAO.delete(999L));
        }

        @Test
        @DisplayName("Should throw DatabaseException when language is still referenced by a race")
        void deleteReferencedByRace()
        {
            Language elvish = (Language) seeded.get("elvish");

            assertThrows(DatabaseException.class, () -> languageDAO.delete(elvish.getId()));
        }
    }

    @Nested
    @DisplayName("GetByName")
    class GetByName
    {
        @Test
        @DisplayName("Should return correct language by name")
        void getByName()
        {
            Language elvish = (Language) seeded.get("elvish");

            Language result = languageDAO.getByName("Elvish");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elvish.getId()));
            assertThat(result.getName(), equalTo("Elvish"));
            assertThat(result.getType(), equalTo(LanguageType.STANDARD));
            assertThat(result.getTypicalSpeakers(), not(empty()));
            assertThat(result.getScript(), notNullValue());
            assertThat(result.getContentHash(), notNullValue());
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should return correct language regardless of input casing")
        void getByNameCaseInsensitive()
        {
            Language elvish = (Language) seeded.get("elvish");

            Language result = languageDAO.getByName("eLvIsH");

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(elvish.getId()));
        }

        @Test
        @DisplayName("Should throw NotFoundException on unknown name")
        void getByNameNotFound()
        {
            assertThrows(NotFoundException.class, () -> languageDAO.getByName("NonExistentLanguage"));
        }
    }

    @Nested
    @DisplayName("GetByNames")
    class GetByNames
    {
        @Test
        @DisplayName("Should return all languages matching the given names")
        void getByNames()
        {
            List<Language> result = languageDAO.getByNames(Set.of("Elvish", "Common"));

            assertThat(result, notNullValue());
            assertThat(result, hasSize(2));
            assertThat(result.stream()
                    .map(Language::getName)
                    .toList(), containsInAnyOrder("Elvish", "Common"));
        }

        @Test
        @DisplayName("Should return results regardless of input casing")
        void getByNamesCaseInsensitive()
        {
            List<Language> result = languageDAO.getByNames(Set.of("eLvIsH", "COMMON"));

            assertThat(result, hasSize(2));
        }

        @Test
        @DisplayName("Should return empty list when no names match")
        void getByNamesNoMatch()
        {
            List<Language> result = languageDAO.getByNames(Set.of("NonExistent"));

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void getByNamesEmpty()
        {
            List<Language> result = languageDAO.getByNames(Set.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("GetAll")
    class GetAll
    {
        @Test
        @DisplayName("Should return all languages")
        void getAll()
        {
            List<Language> result = languageDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, hasSize(TOTAL_NUM_LANGUAGES));
            assertThat(result.stream()
                    .map(Language::getName)
                    .toList(), containsInAnyOrder("Elvish", "Common", "Sylvan"));
        }

        @Test
        @DisplayName("Should return empty list when no languages exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            List<Language> result = languageDAO.getAll();

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }

    @Nested
    @DisplayName("SyncAll")
    class SyncAll
    {
        @Test
        @DisplayName("Should insert new language when name does not exist")
        void syncAllInsert()
        {
            Language newLanguage = new Language(
                    "Dwarvish",
                    "Dwarvish is full of hard consonants and guttural sounds.",
                    LanguageType.STANDARD,
                    List.of("Dwarves"),
                    "Dwarvish",
                    ContentHashing.sha256Hex("Dwarvish")
            );

            List<Language> result = languageDAO.syncAll(List.of(newLanguage));
            List<Language> all = languageDAO.getAll();

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), notNullValue());
            assertThat(result.get(0).getName(), equalTo("Dwarvish"));
            assertThat(all, hasSize(TOTAL_NUM_LANGUAGES + result.size()));
        }

        @Test
        @DisplayName("Should return existing language unchanged when contentHash matches")
        void syncAllSkip()
        {
            Language elvish = (Language) seeded.get("elvish");

            List<Language> result = languageDAO.syncAll(List.of(elvish));
            List<Language> all = languageDAO.getAll();
            Language fetched = languageDAO.getById(elvish.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(elvish.getId()));
            assertThat(all, hasSize(TOTAL_NUM_LANGUAGES));
            assertThat(fetched.getContentHash(), equalTo(elvish.getContentHash()));
        }

        @Test
        @DisplayName("Should update existing language when contentHash differs")
        void syncAllUpdate()
        {
            Language elvish = (Language) seeded.get("elvish");
            Language incoming = new Language(
                    "Elvish",
                    "Updated description.",
                    LanguageType.STANDARD,
                    List.of("Elves"),
                    "Elvish",
                    ContentHashing.sha256Hex("Elvish-updated")
            );

            List<Language> result = languageDAO.syncAll(List.of(incoming));
            List<Language> all = languageDAO.getAll();
            Language fetched = languageDAO.getById(elvish.getId());

            assertThat(result, hasSize(1));
            assertThat(result.get(0).getId(), equalTo(elvish.getId()));
            assertThat(all, hasSize(TOTAL_NUM_LANGUAGES));
            assertThat(fetched.getDescription(), equalTo("Updated description."));
        }

        @Test
        @DisplayName("Should deduplicate incoming list — only one entry per name is persisted")
        void syncAllDeduplication()
        {
            Language first = new Language(
                    "Dwarvish",
                    "First description.",
                    LanguageType.STANDARD,
                    List.of("Dwarves"),
                    "Dwarvish",
                    ContentHashing.sha256Hex("Dwarvish-first")
            );
            Language second = new Language(
                    "Dwarvish",
                    "Second description.",
                    LanguageType.STANDARD,
                    List.of("Dwarves"),
                    "Dwarvish",
                    ContentHashing.sha256Hex("Dwarvish-second")
            );

            languageDAO.syncAll(List.of(first, second));
            List<Language> all = languageDAO.getAll();

            assertThat(all.stream()
                    .filter(language -> language.getName().equals("Dwarvish"))
                    .toList(), hasSize(1));
        }

        @Test
        @DisplayName("Should return empty list when given empty input")
        void syncAllEmpty()
        {
            List<Language> result = languageDAO.syncAll(List.of());

            assertThat(result, notNullValue());
            assertThat(result, empty());
        }
    }
}