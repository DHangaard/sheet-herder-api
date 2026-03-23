package app.persistence.entities.reference;

import app.enums.Ability;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Subrace
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT") // TODO Rethink use of "TEXT"
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "race_id", nullable = false)
    private Race race;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "subrace_ability_bonuses",
            joinColumns = @JoinColumn(name = "subrace_id")
    )
    @MapKeyColumn(name = "ability")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "bonus")
    private Map<Ability, Integer> abilityBonuses;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "subrace_traits",
            joinColumns = @JoinColumn(name = "subrace_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id")
    )
    private Set<Trait> traits;

    @Column(nullable = false, length = 64)
    private String contentHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Subrace(String name, String description, Race race, Map<Ability, Integer> abilityBonuses, Set<Trait> traits, String contentHash)
    {
        this.name = name;
        this.description = description;
        this.race = race;
        this.abilityBonuses = abilityBonuses;
        this.traits = traits;
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
