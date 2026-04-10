package app.persistence.daos.reference.implementations;

import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.reference.Subrace;
import app.exceptions.DatabaseException;
import app.utils.ContentHashing;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
                rollback(em);
                throw new DatabaseException("Failed to persist subrace", e);
            }
        }
    }

    @Override
    public Subrace getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
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
                    .orElseThrow(() -> new NotFoundException("Subrace with id " + id + " was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find Subrace with id: " + id, e);
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
                rollback(em);
                throw new DatabaseException("Failed to update subrace", e);
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
                Subrace found = em.find(Subrace.class, id);
                validateNull(found, id);
                em.getTransaction().begin();
                em.remove(found);
                em.getTransaction().commit();
                return found.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete subrace with id: " + id, e);
            }
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find subrace with id: " + id, e);
        }
    }

    @Override
    public Subrace getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
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
                    .orElseThrow(() -> new NotFoundException("Subrace with name \"" + name + "\" was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find subrace with name: \"" + name + "\"", e);
        }
    }

    @Override
    public List<Subrace> getByNames(Collection<String> names)
    {
        Set<String> keys = normalizeToKeys(names);
        if (keys.isEmpty())
        {
            return List.of();
        }

        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT s
                                FROM Subrace s
                                LEFT JOIN FETCH s.race r
                                LEFT JOIN FETCH r.languages
                                LEFT JOIN FETCH r.traits
                                LEFT JOIN FETCH s.traits
                                WHERE LOWER(s.name) IN :names
                                """, Subrace.class)
                        .setParameter("names", keys)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch subraces by names", e);
            }
        }
    }

    @Override
    public List<Subrace> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT DISTINCT s
                                FROM Subrace s
                                LEFT JOIN FETCH s.race r
                                LEFT JOIN FETCH r.languages
                                LEFT JOIN FETCH r.traits
                                LEFT JOIN FETCH s.traits
                                """, Subrace.class)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch subraces", e);
            }
        }
    }

    // TODO: syncAll follows the same pattern in LanguageDAO, TraitDAO, RaceDAO and SubraceDAO - candidate for AbstractSyncDAO<T> refactor
    @Override
    public List<Subrace> syncAll(List<Subrace> subraces)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                List<Subrace> result = processSync(em, subraces);
                em.getTransaction().commit();
                log.info("Subrace sync complete — {} records processed", result.size());
                return result;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to synchronise subraces", e);
            }
        }
    }

    private Set<String> normalizeToKeys(Collection<String> names)
    {
        if (names == null || names.isEmpty())
        {
            return Set.of();
        }
        return names.stream()
                .filter(Objects::nonNull)
                .map(ContentHashing::normalizeLower)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    private List<Subrace> processSync(EntityManager em, List<Subrace> subraces)
    {
        Map<String, Subrace> incomingByKey = deduplicateByName(subraces);
        if (incomingByKey.isEmpty())
        {
            return List.of();
        }

        Map<String, Subrace> existingByKey = fetchExistingByName(em, incomingByKey.keySet());

        List<Subrace> result = new ArrayList<>();
        for (Map.Entry<String, Subrace> entry : incomingByKey.entrySet())
        {
            Subrace resolved = resolveSubrace(em, entry.getValue(), existingByKey.get(entry.getKey()));
            result.add(resolved);
        }
        return result;
    }

    private Map<String, Subrace> deduplicateByName(List<Subrace> subraces)
    {
        Map<String, Subrace> byKey = new HashMap<>();
        for (Subrace subrace : subraces)
        {
            if (subrace == null || subrace.getName() == null || subrace.getName().isBlank())
            {
                continue;
            }
            byKey.put(ContentHashing.normalizeLower(subrace.getName()), subrace);
        }
        return byKey;
    }

    private Map<String, Subrace> fetchExistingByName(EntityManager em, Set<String> names)
    {
        return em.createQuery("""
                        SELECT s
                        FROM Subrace s
                        WHERE LOWER(s.name) IN :names
                        """, Subrace.class)
                .setParameter("names", names)
                .getResultStream()
                .collect(Collectors.toMap(
                        subrace -> ContentHashing.normalizeLower(subrace.getName()),
                        Function.identity()
                ));
    }

    private Subrace resolveSubrace(EntityManager em, Subrace incoming, Subrace existing)
    {
        if (existing == null)
        {
            em.persist(incoming);
            log.info("New subrace persisted: {}", incoming.getName());
            return incoming;
        }
        if (incoming.getContentHash().equals(existing.getContentHash()))
        {
            log.debug("Skipped unchanged subrace: {}", existing.getName());
            return existing;
        }
        log.info("Subrace updated: {}", existing.getName());
        return applyUpdate(existing, incoming);
    }

    private Subrace applyUpdate(Subrace existing, Subrace incoming)
    {
        existing.setDescription(incoming.getDescription());
        existing.setAbilityBonuses(incoming.getAbilityBonuses());
        existing.setRace(incoming.getRace());
        existing.setTraits(incoming.getTraits());
        existing.setContentHash(incoming.getContentHash());

        return existing;
    }

    private <T> void validateNull(Subrace subrace, T searchParameter)
    {
        if (subrace == null)
        {
            throw new NotFoundException("Subrace not found: " + searchParameter);
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
