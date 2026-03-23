package app.persistence.entities.reference;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Trait
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "trait_descriptions",
            joinColumns = @JoinColumn(name = "trait_id")
    )
    @Column(name = "description", columnDefinition = "TEXT") // TODO Rethink use of "TEXT"
    private List<String> descriptions; // TODO Check for instances of multiple descriptions or refactor

    @Column(nullable = false, length = 64)
    private String contentHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Trait(String name, List<String> descriptions, String contentHash)
    {
        this.name = name;
        this.descriptions = descriptions;
        this.contentHash = contentHash;
    }

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
    }
}
