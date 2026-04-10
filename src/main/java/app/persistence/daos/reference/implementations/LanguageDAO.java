package app.persistence.daos.reference.implementations;

import app.exceptions.NotFoundException;
import app.persistence.daos.reference.interfaces.IReferenceDAO;
import app.persistence.entities.reference.Language;
import app.exceptions.DatabaseException;
import app.utils.ContentHashing;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class LanguageDAO implements IReferenceDAO<Language>
{
    private final EntityManagerFactory emf;

    public LanguageDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public Language create(Language language)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(language);
                em.getTransaction().commit();
                return language;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to persist language", e);
            }
        }
    }

    @Override
    public Language getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Language found = em.find(Language.class, id);
            validateNull(found, id);
            return found;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find Language with id: " + id, e);
        }
    }

    @Override
    public Language update(Language language)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                Language updatedLanguage = em.merge(language);
                em.getTransaction().commit();
                return updatedLanguage;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to update language", e);
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
                Language found = em.find(Language.class, id);
                validateNull(found, id);
                em.getTransaction().begin();
                em.remove(found);
                em.getTransaction().commit();
                return found.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete language with id: " + id, e);
            }
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find language with id: " + id, e);
        }
    }

    @Override
    public Language getByName(String name)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
                            SELECT l
                            FROM Language l
                            WHERE LOWER(l.name) = LOWER(:name)
                            """, Language.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Language with name \"" + name + "\" was not found"));
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find language with name: \"" + name + "\"", e);
        }
    }

    @Override
    public List<Language> getByNames(Collection<String> names)
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
                                SELECT l
                                FROM Language l
                                WHERE LOWER(l.name) IN :names
                                """, Language.class)
                        .setParameter("names", keys)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch languages by names", e);
            }
        }
    }

    @Override
    public List<Language> getAll()
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                return em.createQuery("""
                                SELECT l
                                FROM Language l
                                """, Language.class)
                        .getResultList();
            }
            catch (PersistenceException e)
            {
                throw new DatabaseException("Failed to fetch languages", e);
            }
        }
    }

    // TODO: syncAll follows the same pattern in LanguageDAO, TraitDAO, RaceDAO and SubraceDAO - candidate for AbstractSyncDAO<T> refactor
    @Override
    public List<Language> syncAll(List<Language> languages)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                List<Language> result = processSync(em, languages);
                em.getTransaction().commit();
                log.info("Language sync complete — {} records processed", result.size());
                return result;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to synchronise languages", e);
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

    private List<Language> processSync(EntityManager em, List<Language> languages)
    {
        Map<String, Language> incomingByKey = deduplicateByName(languages);
        if (incomingByKey.isEmpty())
        {
            return List.of();
        }

        Map<String, Language> existingByKey = fetchExistingByName(em, incomingByKey.keySet());

        List<Language> result = new ArrayList<>();
        for (Map.Entry<String, Language> entry : incomingByKey.entrySet())
        {
            Language resolved = resolveLanguage(em, entry.getValue(), existingByKey.get(entry.getKey()));
            result.add(resolved);
        }
        return result;
    }

    private Map<String, Language> deduplicateByName(List<Language> languages)
    {
        Map<String, Language> byKey = new HashMap<>();
        for (Language language : languages)
        {
            if (language == null || language.getName() == null || language.getName().isBlank())
            {
                continue;
            }
            byKey.put(ContentHashing.normalizeLower(language.getName()), language);
        }
        return byKey;
    }

    private Map<String, Language> fetchExistingByName(EntityManager em, Set<String> names)
    {
        return em.createQuery("""
                        SELECT l
                        FROM Language l
                        WHERE LOWER(l.name) IN :names
                        """, Language.class)
                .setParameter("names", names)
                .getResultStream()
                .collect(Collectors.toMap(
                        language -> ContentHashing.normalizeLower(language.getName()),
                        Function.identity()
                ));
    }

    private Language resolveLanguage(EntityManager em, Language incoming, Language existing)
    {
        if (existing == null)
        {
            em.persist(incoming);
            log.info("New language persisted: {}", incoming.getName());
            return incoming;
        }
        if (incoming.getContentHash().equals(existing.getContentHash()))
        {
            log.debug("Skipped unchanged language: {}", existing.getName());
            return existing;
        }
        log.info("Language updated: {}", existing.getName());
        return applyUpdate(existing, incoming);
    }

    private Language applyUpdate(Language existing, Language incoming)
    {
        existing.setDescription(incoming.getDescription());
        existing.setType(incoming.getType());
        existing.setTypicalSpeakers(incoming.getTypicalSpeakers());
        existing.setScript(incoming.getScript());
        existing.setContentHash(incoming.getContentHash());
        return existing;
    }

    private <T> void validateNull(Language language, T searchParameter)
    {
        if (language == null)
        {
            throw new NotFoundException("Language not found: " + searchParameter);
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
