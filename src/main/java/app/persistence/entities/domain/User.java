package app.persistence.entities.domain;

import app.security.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "hashed_password", nullable = false, unique = false, length = 60)
    private String hashedPassword;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>(Set.of(Role.USER));

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User(String email, String username, String hashedPassword)
    {
        this.email = email;
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.email = this.email.toLowerCase().trim();
        this.username = normalizeName(this.username);
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
        this.email = this.email.toLowerCase().trim();
        this.username = normalizeName(this.username);
    }

    public boolean addRole(Role role)
    {
        if (roles.contains(role))
        {
            return false;
        }
        roles.add(role);
        return true;
    }

    public boolean removeRole(Role role)
    {
        if (!roles.contains(role))
        {
            return false;
        }
        roles.remove(role);
        return true;
    }

    private String normalizeName(String name)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        return name.trim();
    }
}


