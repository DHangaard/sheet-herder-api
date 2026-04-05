package app.persistence.daos.domain.implementations;

import app.config.hibernate.HibernateTestConfig;
import app.exceptions.ConflictException;
import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.daos.domain.interfaces.IUserDAO;
import app.persistence.entities.IEntity;
import app.persistence.entities.domain.User;
import app.security.enums.Role;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOTest
{
    private final EntityManagerFactory emf = HibernateTestConfig.getEntityManagerFactory();
    private IUserDAO userDAO;
    private ICharacterSheetDAO characterSheetDAO;
    private Map<String, IEntity> seeded;

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        TestPopulator testPopulator = new TestPopulator(emf);
        testPopulator.populate();
        seeded = testPopulator.getSeededData();
        userDAO = new UserDAO(emf);
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
        @DisplayName("Should persist user, normalize email and username, and set timestamps")
        void create()
        {
            User newUser = new User(" NEW@TEST.COM ", " Gandalf ", "Password_99");

            User result = userDAO.create(newUser);

            assertThat(result, notNullValue());
            assertThat(result.getId(), notNullValue());
            assertThat(result.getEmail(), equalTo("new@test.com"));
            assertThat(result.getUsername(), equalTo("Gandalf"));
            assertThat(result.getRoles(), hasItem(Role.USER));
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate email")
        void createDuplicateEmail()
        {
            User duplicate = new User("john@test.com", "unique_username", "Password_99");

            assertThrows(DatabaseException.class, () -> userDAO.create(duplicate));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate username")
        void createDuplicateUsername()
        {
            User duplicate = new User("unique@test.com", "john", "Password_99");

            assertThrows(DatabaseException.class, () -> userDAO.create(duplicate));
        }
    }

    @Nested
    @DisplayName("GetById")
    class GetById
    {
        @Test
        @DisplayName("Should return correct user by id")
        void getById()
        {
            User john = (User) seeded.get("john");

            User result = userDAO.getById(john.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(john.getId()));
            assertThat(result.getEmail(), equalTo("john@test.com"));
            assertThat(result.getUsername(), equalTo("john"));
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
        }

        @Test
        @DisplayName("Should throw NotFoundException when id does not exist")
        void getByIdNotFound()
        {
            assertThrows(NotFoundException.class, () -> userDAO.getById(999L));
        }
    }

    @Nested
    @DisplayName("Update")
    class Update
    {
        @Test
        @DisplayName("Should persist updated (normalized) fields and set new updatedAt timestamp")
        void update()
        {
            User john = (User) seeded.get("john");
            john.setEmail(" JOHN_UPDATED@TEST.COM ");
            john.setUsername(" john_updated ");
            userDAO.update(john);

            User result = userDAO.getById(john.getId());

            assertThat(result, notNullValue());
            assertThat(result.getId(), equalTo(john.getId()));
            assertThat(result.getEmail(), equalTo("john_updated@test.com"));
            assertThat(result.getUsername(), equalTo("john_updated"));
            assertThat(result.getCreatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), notNullValue());
            assertThat(result.getUpdatedAt(), not(equalTo(result.getCreatedAt())));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate email")
        void updateDuplicateEmail()
        {
            User john = (User) seeded.get("john");

            john.setEmail("morten@test.com");

            assertThrows(DatabaseException.class, () -> userDAO.update(john));
        }

        @Test
        @DisplayName("Should throw DatabaseException on duplicate username")
        void updateDuplicateUsername()
        {
            User john = (User) seeded.get("john");

            john.setUsername("morten");

            assertThrows(DatabaseException.class, () -> userDAO.update(john));
        }
    }

    @Nested
    @DisplayName("Delete")
    class Delete
    {
        @Test
        @DisplayName("Should delete user and throw NotFoundException when fetched afterwards")
        void delete()
        {
            User gary = (User) seeded.get("gary");

            Long result = userDAO.delete(gary.getId());

            assertThat(result, notNullValue());
            assertThat(result, equalTo(gary.getId()));
            assertThrows(NotFoundException.class, () -> userDAO.getById(result));
        }

        @Test
        @DisplayName("Should throw NotFoundException when id does not exist")
        void deleteNotFound()
        {
            assertThrows(NotFoundException.class, () -> userDAO.delete(999L));
        }

        @Test
        @DisplayName("Should cascade delete all character sheets owned by the user")
        void deleteCascadesCharacterSheets()
        {
            User john = (User) seeded.get("john");
            Long johnSheet1Id = seeded.get("johnSheet1").getId();
            Long johnSheet2Id = seeded.get("johnSheet2").getId();

            userDAO.delete(john.getId());

            assertThrows(NotFoundException.class, () -> userDAO.getById(john.getId()));
            assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(johnSheet1Id));
            assertThrows(NotFoundException.class, () -> characterSheetDAO.getById(johnSheet2Id));
        }
    }

    @Nested
    @DisplayName("GetByEmail")
    class GetByEmail
    {
        @Test
        @DisplayName("Should return populated Optional when email exists")
        void getByEmail()
        {
            Optional<User> result = userDAO.getByEmail("john@test.com");

            assertThat(result.isPresent(), is(true));
            assertThat(result.get().getEmail(), equalTo("john@test.com"));
        }

        @Test
        @DisplayName("Should return empty Optional when email does not exist")
        void getByEmailNotFound()
        {
            Optional<User> result = userDAO.getByEmail("nobody@test.com");

            assertThat(result.isPresent(), is(false));
        }
    }

    @Nested
    @DisplayName("GetByUsername")
    class GetByUsername
    {
        @Test
        @DisplayName("Should return populated Optional when username exists")
        void getByUsername()
        {
            Optional<User> result = userDAO.getByUsername("john");

            assertThat(result.isPresent(), is(true));
            assertThat(result.get().getUsername(), equalTo("john"));
        }

        @Test
        @DisplayName("Should return empty Optional when username does not exist")
        void getByUsernameNotFound()
        {
            Optional<User> result = userDAO.getByUsername("nobody");

            assertThat(result.isPresent(), is(false));
        }
    }

    @Nested
    @DisplayName("AddRole")
    class AddRole
    {
        @Test
        @DisplayName("Should add role to user")
        void addRole()
        {
            User gary = (User) seeded.get("gary");

            User result = userDAO.addRole(gary.getId(), Role.ADMIN);

            assertThat(result.getRoles(), hasItem(Role.ADMIN));
        }

        @Test
        @DisplayName("Should throw ConflictException when role already exists on user")
        void addRoleDuplicate()
        {
            User john = (User) seeded.get("john");

            assertThrows(ConflictException.class, () -> userDAO.addRole(john.getId(), Role.USER));
        }
    }

    @Nested
    @DisplayName("RemoveRole")
    class RemoveRole
    {
        @Test
        @DisplayName("Should remove role from user")
        void removeRole()
        {
            User john = (User) seeded.get("john");

            User result = userDAO.removeRole(john.getId(), Role.USER);

            assertThat(result.getRoles(), not(hasItem(Role.USER)));
        }

        @Test
        @DisplayName("Should throw ConflictException when role is not present on user")
        void removeRoleNotPresent()
        {
            User john = (User) seeded.get("john");

            assertThrows(ConflictException.class, () -> userDAO.removeRole(john.getId(), Role.ADMIN));
        }
    }
}