package app.persistence.daos.reference.implementations;

import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.reference.Trait;
import app.exceptions.DatabaseException;
import app.utils.ContentHashing;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
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
                rollback(em);
                throw new DatabaseException("Failed to persist trait", e);
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
                throw new NotFoundException("Trait not found - id: " + id);
            }
            return foundTrait;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find Trait with id: " + id, e);
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
                rollback(em);
                throw new DatabaseException("Failed to update trait", e);
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
                throw new NotFoundException("Trait not found - id: " + id);
            }
            try
            {
                em.remove(foundTrait);
                em.getTransaction().commit();
                return foundTrait.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete trait with id: " + id, e);
            }
        }
    }

    @Override
    public Trait getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
                            SELECT t
                            FROM Trait t
                            WHERE LOWER(t.name) = LOWER(:name)
                            """, Trait.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Trait with name \"" + name + "\" was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find trait with name: \"" + name + "\"", e);
        }
    }

    @Override
    public List<Trait> getByNames(Collection<String> names)
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
                                SELECT t
                                FROM Trait t
                                WHERE LOWER(t.name) IN :names
                                """, Trait.class)
                        .setParameter("names", keys)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch traits by names", e);
            }
        }
    }

    @Override
    public List<Trait> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT t
                                FROM Trait t
                                """, Trait.class)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch traits", e);
            }
        }
    }

    // TODO: syncAll follows the same pattern in LanguageDAO, TraitDAO, RaceDAO and SubraceDAO - candidate for AbstractSyncDAO<T> refactor
    @Override
    public List<Trait> syncAll(List<Trait> traits)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                List<Trait> result = processSync(em, traits);
                em.getTransaction().commit();
                log.info("Trait sync complete — {} records processed", result.size());
                return result;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to synchronise traits", e);
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

    private List<Trait> processSync(EntityManager em, List<Trait> traits)
    {
        Map<String, Trait> incomingByKey = deduplicateByName(traits);
        if (incomingByKey.isEmpty())
        {
            return List.of();
        }

        Map<String, Trait> existingByKey = fetchExistingByName(em, incomingByKey.keySet());

        List<Trait> result = new ArrayList<>();
        for (Map.Entry<String, Trait> entry : incomingByKey.entrySet())
        {
            Trait resolved = resolveTrait(em, entry.getValue(), existingByKey.get(entry.getKey()));
            result.add(resolved);
        }
        return result;
    }

    private Map<String, Trait> deduplicateByName(List<Trait> traits)
    {
        Map<String, Trait> byKey = new HashMap<>();
        for (Trait trait : traits)
        {
            if (trait == null || trait.getName() == null || trait.getName().isBlank())
            {
                continue;
            }
            byKey.put(ContentHashing.normalizeLower(trait.getName()), trait);
        }
        return byKey;
    }

    private Map<String, Trait> fetchExistingByName(EntityManager em, Set<String> names)
    {
        return em.createQuery("""
                        SELECT t
                        FROM Trait t
                        WHERE LOWER(t.name) IN :names
                        """, Trait.class)
                .setParameter("names", names)
                .getResultStream()
                .collect(Collectors.toMap(
                        trait -> ContentHashing.normalizeLower(trait.getName()),
                        Function.identity()
                ));
    }

    private Trait resolveTrait(EntityManager em, Trait incoming, Trait existing)
    {
        if (existing == null)
        {
            em.persist(incoming);
            log.info("New trait persisted: {}", incoming.getName());
            return incoming;
        }
        if (incoming.getContentHash().equals(existing.getContentHash()))
        {
            log.debug("Skipped unchanged trait: {}", existing.getName());
            return existing;
        }
        log.info("Trait updated: {}", existing.getName());
        return applyUpdate(existing, incoming);
    }

    private Trait applyUpdate(Trait existing, Trait incoming)
    {
        existing.setDescriptions(incoming.getDescriptions());
        existing.setContentHash(incoming.getContentHash());
        return existing;
    }

    private void rollback(EntityManager em)
    {
        if (em.getTransaction().isActive())
        {
            em.getTransaction().rollback();
        }
    }
}
