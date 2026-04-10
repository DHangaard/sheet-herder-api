package app.testutils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

public class TestCleanDB
{
    private TestCleanDB()
    {
    }

    public static void truncateTables(EntityManagerFactory emf)
    {
        if (emf == null || !emf.isOpen())
            throw new IllegalStateException("EMF is closed in TestCleanDB");

        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createNativeQuery("""
                        TRUNCATE TABLE
                            character_languages,
                            character_ability_scores,
                            character_notes,
                            character_sheet,
                            user_roles,
                            users,
                            trait_descriptions,
                            trait,
                            subrace_traits,
                            subrace_ability_bonuses,
                            subrace,
                            race_traits,
                            race_languages,
                            race_ability_bonuses,
                            race,
                            language_typical_speakers,
                            language
                        RESTART IDENTITY CASCADE
                    """).executeUpdate();
            em.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new RuntimeException("Failed to truncate tables", e);
        }
    }
}