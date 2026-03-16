package app.persistence;

import app.entities.reference.Subrace;
import app.exceptions.DatabaseException;
import jakarta.persistence.*;

import java.util.List;

public class SubraceDAO implements IReferenceDAO<Subrace>
{
    private final EntityManagerFactory emf;

    public SubraceDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Subrace create(Subrace subrace)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(subrace);
                em.getTransaction().commit();
                return subrace;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to save subrace: " + e.getMessage());
            }
        }
    }

    @Override
    public Subrace getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Subrace foundSubrace = em.createQuery("""
                            SELECT s
                            FROM Subrace s
                            LEFT JOIN FETCH s.race r
                            LEFT JOIN FETCH r.languages
                            LEFT JOIN FETCH r.traits
                            LEFT JOIN FETCH s.traits
                            WHERE s.id = :id
                            """, Subrace.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Subrace with id " + id + " was not found"));
            return foundSubrace;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find Subrace with id: " + id + " " + e.getMessage());
        }
    }

    @Override
    public List<Subrace> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                TypedQuery<Subrace> query = em.createQuery("""
                        SELECT DISTINCT s
                        FROM Subrace s
                        LEFT JOIN FETCH s.race r
                        LEFT JOIN FETCH r.languages
                        LEFT JOIN FETCH r.traits
                        LEFT JOIN FETCH s.traits
                        """, Subrace.class);
                return query.getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch subraces: " + e.getMessage());
            }
        }
    }

    @Override
    public Subrace update(Subrace subrace)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Subrace updatedSubrace = em.merge(subrace);
                em.getTransaction().commit();
                return updatedSubrace;
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to update subrace: " + e.getMessage());
            }
        }
    }

    @Override
    public Long delete(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            Subrace foundSubrace = em.find(Subrace.class, id);
            if (foundSubrace == null)
            {
                throw new DatabaseException("Subrace not found - id: " + id);
            }
            try
            {
                em.remove(foundSubrace);
                em.getTransaction().commit();
                return foundSubrace.getId();
            }
            catch (PersistenceException e)
            {
                if (em.getTransaction().isActive())
                {
                    em.getTransaction().rollback();
                }
                throw new DatabaseException("Failed to delete subrace with id: " + id + " " + e.getMessage());
            }
        }
    }

    @Override
    public Subrace getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Subrace foundSubrace = em.createQuery("""
                            SELECT s
                            FROM Subrace s
                            LEFT JOIN FETCH s.race r
                            LEFT JOIN FETCH r.languages
                            LEFT JOIN FETCH r.traits
                            LEFT JOIN FETCH s.traits
                            WHERE LOWER(s.name) = LOWER(:name)
                            """, Subrace.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Subrace with name \"" + name + "\" was not found"));
            return foundSubrace;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find subrace with name: \"" + name + "\"" + e.getMessage());
        }
    }
}
