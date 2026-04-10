package app.testutils;

import app.persistence.entities.IEntity;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import app.persistence.daos.domain.implementations.CharacterSheetDAO;
import app.persistence.daos.domain.implementations.UserDAO;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.daos.domain.interfaces.IUserDAO;
import app.security.utils.PasswordUtil;
import jakarta.persistence.EntityManagerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TestPopulator
{
    private final IUserDAO userDAO;
    private final ICharacterSheetDAO characterSheetDAO;
    private final Map<String, IEntity> seeded;

    private static final int TEST_BCRYPT_COST = 4;

    public TestPopulator(EntityManagerFactory emf)
    {
        this.userDAO = new UserDAO(emf);
        this.characterSheetDAO = new CharacterSheetDAO(emf);
        this.seeded = new HashMap<>();
    }

    public void populate()
    {
        populateUsers();
        populateCharacterSheets();
    }

    public Map<String, IEntity> getSeededData()
    {
        return Collections.unmodifiableMap(seeded);
    }

    private void populateUsers()
    {
        User john = new User("john@test.com", "john", PasswordUtil.hashPassword("Password_1", TEST_BCRYPT_COST));
        User morten = new User("morten@test.com", "morten", PasswordUtil.hashPassword("Password_2", TEST_BCRYPT_COST));
        User gary = new User("gary@test.com", "gary", PasswordUtil.hashPassword("Password_3", TEST_BCRYPT_COST));

        userDAO.create(john);
        userDAO.create(morten);
        userDAO.create(gary);

        seeded.put("john", john);
        seeded.put("morten", morten);
        seeded.put("gary", gary);
    }

    private void populateCharacterSheets()
    {
        User john = (User) seeded.get("john");
        User morten = (User) seeded.get("morten");

        CharacterSheet johnSheet1 = new CharacterSheet(john, "Aragorn", null, null, Set.of(), Map.of());
        CharacterSheet johnSheet2 = new CharacterSheet(john, "Legolas", null, null, Set.of(), Map.of());
        CharacterSheet mortenSheet1 = new CharacterSheet(morten, "Gimli", null, null, Set.of(), Map.of());

        characterSheetDAO.create(johnSheet1);
        characterSheetDAO.create(johnSheet2);
        characterSheetDAO.create(mortenSheet1);

        seeded.put("johnSheet1", johnSheet1);
        seeded.put("johnSheet2", johnSheet2);
        seeded.put("mortenSheet1", mortenSheet1);
    }
}