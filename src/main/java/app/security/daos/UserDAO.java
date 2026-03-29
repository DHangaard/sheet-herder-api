package app.security.daos;

import app.exceptions.DatabaseException;
import app.persistence.entities.domain.User;
import app.security.enums.Role;
import app.exceptions.ConflictException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;

import java.util.Optional;

public class UserDAO implements IUserDAO
{
    private final EntityManagerFactory emf;

    public UserDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User create(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                em.persist(user);
                em.getTransaction().commit();
                return user;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to save user: " + e.getMessage());
            }
        }
    }

    @Override
    public Optional<User> getByEmail(String email)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            return em.createQuery("""
                            SELECT u
                            FROM User u
                            WHERE LOWER(u.email) = LOWER(:email) 
                            """, User.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find user with email: " + email + " " + e.getMessage());
        }
    }

    @Override
    public User getById(Long id)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User foundUser = em.find(User.class, id);
            validateNull(foundUser, id);
            return foundUser;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Failed to find user with id: " + id + " " + e.getMessage());
        }
    }

    @Override
    public User update(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                User updatedUser = em.merge(user);
                em.getTransaction().commit();
                return updatedUser;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to update user: " + e.getMessage());
            }
        }
    }

    @Override
    public User addRole(Long id, Role role)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                User foundUser = em.find(User.class, id);
                validateNull(foundUser, id);

                if (!foundUser.addRole(role))
                {
                    throw new ConflictException("User " + id + " already contains role: " + role);
                }

                em.getTransaction().commit();
                return foundUser;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to add role \"" + role + "\" user: " + e.getMessage());
            }
        }
    }

    @Override
    public User removeRole(Long id, Role role)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            try
            {
                em.getTransaction().begin();
                User foundUser = em.find(User.class, id);
                validateNull(foundUser, id);

                if (!foundUser.removeRole(role))
                {
                    throw new ConflictException("User " + id + " does not contain role: " + role);
                }

                em.getTransaction().commit();
                return foundUser;
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to remove role \"" + role + "\" user: " + e.getMessage());
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
                em.getTransaction().begin();
                User foundUser = em.find(User.class, id);
                validateNull(foundUser, id);
                em.remove(foundUser);
                em.getTransaction().commit();
                return foundUser.getId();
            }
            catch (PersistenceException e)
            {
                rollback(em);
                throw new DatabaseException("Failed to delete user with id: " + id + " " + e.getMessage());
            }
        }
    }

    private <T> void validateNull(User user, T searchParameter)
    {
        if (user == null)
        {
            throw new EntityNotFoundException("User not found: " + searchParameter);
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
