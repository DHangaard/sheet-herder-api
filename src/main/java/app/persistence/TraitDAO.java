package app.persistence;

import app.entities.reference.Language;
import app.entities.reference.Trait;
import app.exceptions.DatabaseException;
import jakarta.persistence.*;

import java.util.List;

public class TraitDAO implements IReferenceDAO<Trait>
{
    private final EntityManagerFactory emf;

    public TraitDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Trait create(Trait trait)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(trait);
                em.getTransaction().commit();
                return trait;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to save trait: " + e.getMessage());
            }
        }
    }

    @Override
    public Trait getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Trait foundTrait = em.find(Trait.class, id);
            if (foundTrait == null)
            {
                throw new DatabaseException("Trait not found - id: " + id);
            }
            return foundTrait;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find Trait with id: " + id + " " + e.getMessage());
        }
    }

    @Override
    public List<Trait> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Trait> query = em.createQuery("SELECT t FROM Trait t", Trait.class);
                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch traits: " + e.getMessage());
            }
        }
    }

    @Override
    public Trait update(Trait trait)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Trait updatedTrait = em.merge(trait);
                em.getTransaction().commit();
                return updatedTrait;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to update trait: " + e.getMessage());
            }
        }
    }

    @Override
    public Long delete(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            Trait foundTrait = em.find(Trait.class, id);
            if (foundTrait == null)
            {
                throw new DatabaseException("Trait not found - id: " + id);
            }
            try
            {
                em.remove(foundTrait);
                em.getTransaction().commit();
                return foundTrait.getId();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to delete trait with id: " + id + " " + e.getMessage());
            }
        }
    }

    @Override
    public Trait getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Trait foundTrait = em.createQuery("SELECT t FROM Trait t WHERE LOWER(t.name) = LOWER(:name)", Trait.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Trait with name \"" + name + "\" was not found"));
            return foundTrait;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find trait with name: \"" + name + "\"" + e.getMessage());
        }
    }
}
