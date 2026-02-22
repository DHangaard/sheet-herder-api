package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
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

    private LocalDate createdAt;
    private LocalDate updatedAt;

    // Password will be added in the future when authentication is implemented

    public User(String email, String username)
    {
        this.email = email;
        this.username = username;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.email.toLowerCase().trim();
        this.username.trim();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDate.now();
        this.email.toLowerCase().trim();
        this.username.trim();
    }
}


