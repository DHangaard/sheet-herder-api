package app.entities.reference;

import app.enums.Ability;
import app.enums.Size;
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
public class Race
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private int speed;

    @ElementCollection
    @CollectionTable(
            name = "race_ability_bonuses",
            joinColumns = @JoinColumn(name = "race_id")
    )
    @MapKeyColumn(name = "ability")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "bonus")
    private Map<Ability, Integer> abilityBonuses;

    @Column(columnDefinition = "TEXT") // TODO Rethink use of "TEXT"
    private String ageDescription;

    @Column(columnDefinition = "TEXT") // TODO Rethink use of "TEXT"
    private String alignment;

    @Enumerated(EnumType.STRING)
    private Size size;

    private String sizeDescription;

    @ManyToMany
    @JoinTable(
            name = "race_languages",
            joinColumns = @JoinColumn(name = "race_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private Set<Language> languages;

    @Column(columnDefinition = "TEXT") // TODO Rethink use of "TEXT"
    private String languageDescription;

    @ManyToMany
    @JoinTable(
            name = "race_traits",
            joinColumns = @JoinColumn(name = "race_id"),
            inverseJoinColumns = @JoinColumn(name = "trait_id")
    )
    private Set<Trait> traits;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Race(String name, int speed, Map<Ability, Integer> abilityBonuses, String ageDescription, String alignment, Size size, String sizeDescription, Set<Language> languages, String languageDescription, Set<Trait> traits)
    {
        this.name = name;
        this.speed = speed;
        this.abilityBonuses = abilityBonuses;
        this.ageDescription = ageDescription;
        this.alignment = alignment;
        this.size = size;
        this.sizeDescription = sizeDescription;
        this.languages = languages;
        this.languageDescription = languageDescription;
        this.traits = traits;
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
