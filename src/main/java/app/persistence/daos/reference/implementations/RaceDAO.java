package app.persistence.daos.reference.implementations;

import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.reference.Race;
import app.exceptions.DatabaseException;
import app.utils.ContentHashing;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class RaceDAO implements IReferenceDAO<Race>
{
    private final EntityManagerFactory emf;

    public RaceDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Race create(Race race)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(race);
                em.getTransaction().commit();
                return race;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to persist race", e);
            }
        }
    }

    @Override
    public Race getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
                            SELECT r
                            FROM Race r
                            LEFT JOIN FETCH r.languages
                            LEFT JOIN FETCH r.traits
                            WHERE r.id = :id
                            """, Race.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Race with id " + id + " was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find race with name: " + id, e);
        }
    }

    @Override
    public Race update(Race race)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Race updatedRace = em.merge(race);
                em.getTransaction().commit();
                return updatedRace;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to update race", e);
            }
        }
    }

    @Override
    public Long delete(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            Race foundRace = em.find(Race.class, id);
            if (foundRace == null)
            {
                throw new NotFoundException("Race not found - id: " + id);
            }
            try
            {
                em.remove(foundRace);
                em.getTransaction().commit();
                return foundRace.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete race with id: " + id, e);
            }
        }
    }

    @Override
    public Race getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
                            SELECT r
                            FROM Race r
                            LEFT JOIN FETCH r.languages
                            LEFT JOIN FETCH r.traits
                            WHERE LOWER(r.name) = LOWER(:name)""", Race.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Race with name \"" + name + "\" was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find race with name: \"" + name + "\"", e);
        }
    }

    @Override
    public List<Race> getByNames(Collection<String> names)
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
                                SELECT DISTINCT r
                                FROM Race r
                                LEFT JOIN FETCH r.languages
                                LEFT JOIN FETCH r.traits
                                WHERE LOWER(r.name) IN :names
                                """, Race.class)
                        .setParameter("names", keys)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch races by names", e);
            }
        }
    }

    @Override
    public List<Race> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT DISTINCT r
                                FROM Race r
                                LEFT JOIN FETCH r.languages
                                LEFT JOIN FETCH r.traits
                                """, Race.class)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch races", e);
            }
        }
    }

    // TODO: syncAll follows the same pattern in LanguageDAO, TraitDAO, RaceDAO and SubraceDAO - candidate for AbstractSyncDAO<T> refactor
    @Override
    public List<Race> syncAll(List<Race> races)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                List<Race> result = processSync(em, races);
                em.getTransaction().commit();
                log.info("Race sync complete — {} records processed", result.size());
                return result;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to synchronise races", e);
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

    private List<Race> processSync(EntityManager em, List<Race> races)
    {
        Map<String, Race> incomingByKey = deduplicateByName(races);
        if (incomingByKey.isEmpty())
        {
            return List.of();
        }

        Map<String, Race> existingByKey = fetchExistingByName(em, incomingByKey.keySet());

        List<Race> result = new ArrayList<>();
        for (Map.Entry<String, Race> entry : incomingByKey.entrySet())
        {
            Race resolved = resolveRace(em, entry.getValue(), existingByKey.get(entry.getKey()));
            result.add(resolved);
        }
        return result;
    }

    private Map<String, Race> deduplicateByName(List<Race> races)
    {
        Map<String, Race> byKey = new HashMap<>();
        for (Race race : races)
        {
            if (race == null || race.getName() == null || race.getName().isBlank())
            {
                continue;
            }
            byKey.put(ContentHashing.normalizeLower(race.getName()), race);
        }
        return byKey;
    }

    private Map<String, Race> fetchExistingByName(EntityManager em, Set<String> names)
    {
        return em.createQuery("""
                        SELECT r
                        FROM Race r
                        WHERE LOWER(r.name) IN :names
                        """, Race.class)
                .setParameter("names", names)
                .getResultStream()
                .collect(Collectors.toMap(
                        race -> ContentHashing.normalizeLower(race.getName()),
                        Function.identity()
                ));
    }

    private Race resolveRace(EntityManager em, Race incoming, Race existing)
    {
        if (existing == null)
        {
            em.persist(incoming);
            log.info("New race persisted: {}", incoming.getName());
            return incoming;
        }
        if (incoming.getContentHash().equals(existing.getContentHash()))
        {
            log.debug("Skipped unchanged race: {}", existing.getName());
            return existing;
        }
        log.info("Race updated: {}", existing.getName());
        return applyUpdate(existing, incoming);
    }

    private Race applyUpdate(Race existing, Race incoming)
    {
        existing.setSpeed(incoming.getSpeed());
        existing.setAbilityBonuses(incoming.getAbilityBonuses());
        existing.setAgeDescription(incoming.getAgeDescription());
        existing.setAlignment(incoming.getAlignment());
        existing.setSize(incoming.getSize());
        existing.setSizeDescription(incoming.getSizeDescription());
        existing.setLanguageDescription(incoming.getLanguageDescription());
        existing.setLanguages(incoming.getLanguages());
        existing.setTraits(incoming.getTraits());
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
