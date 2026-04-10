package app.persistence.daos.domain.implementations;

import app.exceptions.DatabaseException;
import app.exceptions.NotFoundException;
import app.persistence.daos.domain.interfaces.ICharacterSheetDAO;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceException;

import java.util.List;

public class CharacterSheetDAO implements ICharacterSheetDAO
{
    private final EntityManagerFactory emf;

    public CharacterSheetDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public CharacterSheet create(CharacterSheet characterSheet)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(characterSheet);
                em.getTransaction().commit();
                return characterSheet;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to persist character sheet", e);
            }
        }
    }

    @Override
    public CharacterSheet getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            CharacterSheet foundCharacterSheet = em.find(CharacterSheet.class, id);
            validateNull(foundCharacterSheet, id);
            return foundCharacterSheet;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find character sheet with id: " + id, e);
        }
    }

    @Override
    public CharacterSheet update(CharacterSheet characterSheet)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                CharacterSheet updatedCharacterSheet = em.merge(characterSheet);
                em.getTransaction().commit();
                return updatedCharacterSheet;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to update character sheet", e);
            }
        }
    }

    @Override
    public Long delete(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                CharacterSheet found = em.find(CharacterSheet.class, id);
                validateNull(found, id);
                em.getTransaction().begin();
                em.remove(found);
                em.getTransaction().commit();
                return found.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete character sheet with id: " + id, e);
            }
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find character sheet with id: " + id, e);
        }
    }

    @Override
    public List<CharacterSheet> getAllByUser(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT c
                                FROM CharacterSheet c
                                WHERE c.user.id = :userId
                                """, CharacterSheet.class)
                        .setParameter("userId", user.getId())
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch character sheets from user: " + user.getId(), e);
            }
        }
    }

    private <T> void validateNull(CharacterSheet characterSheet, T searchParameter)
    {
        if (characterSheet == null)
        {
            throw new NotFoundException("Character sheet not found: " + searchParameter);
        }
    }

    private void rollback(EntityManager em)
    {
        if (em.getTransaction().isActive())
        {
            em.getTransaction().rollback();
        }
    }
}
