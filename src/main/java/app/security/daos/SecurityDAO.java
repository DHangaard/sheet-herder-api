package app.security.daos;


import app.entities.Role;
import app.entities.User;
import app.exceptions.ValidationException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class SecurityDAO implements ISecurityDAO
{
    private final EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    @Override
    public User getVerifiedUser(String username, String password) throws ValidationException
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User user = em.find(User.class, username);
            if (user.verifyPassword(password))
            {
                return user;
            }
            else
            {
                throw new IllegalArgumentException("User could not be validated");
            }
        }
    }

    @Override
    public User createUser(String username, String password)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            User user = new User(username, password);
            Role userRole;

            em.getTransaction().begin();
            userRole = validateRole(em, "user");
            user.addRole(userRole);
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Role createRole(String role)
    {
        return null;
    }

    @Override
    public User addUserRole(String username, String role)
    {
        return null;
    }

    private Role validateRole(EntityManager em, String roleName)
    {
        Role role = em.find(Role.class, roleName);
        if (role == null)
        {
            role = new Role(roleName);
            em.persist(role);
            return role;
        }
        return role;
    }
}
