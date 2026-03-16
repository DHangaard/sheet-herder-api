package app.entities;

import app.entities.reference.*;
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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_user_character_name", columnNames = {"user_id", "name"}))
public class CharacterSheet
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "race_id")
    private Race race;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subrace_id")
    private Subrace subrace;

    @ManyToMany
    @JoinTable(
            name = "character_languages",
            joinColumns = @JoinColumn(name = "character_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private Set<Language> languages;

    @ElementCollection
    @CollectionTable(
            name = "character_ability_scores",
            joinColumns = @JoinColumn(name = "character_id")
    )
    @MapKeyColumn(name = "ability")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "score")
    private Map<Ability, Integer> abilityScores;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.name = normalizeName(this.name);
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDateTime.now();
        this.name = normalizeName(this.name);
    }

    private String normalizeName(String name)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Character name cannot be blank");
        }
        return name.trim();
    }
}

// Implement Character Sheet from Dungeons & Dragons 5th Edition (2024)

// private String appearance;
// private int level;
// private int armorClass;
// private int hitPoints;
// private String hitDice; // TODO Should dice be a class?
// private Set<String, Feat> feats; // TODO Set or Map?
// private boolean heroicInspiration;
// private CharacterClass characterClass;
// private CharacterOrigin characterOrigin;
// private Map<String, Proficiency> proficiencies; // TODO Set or Map?
// private Background background;
// private Set<String, AbilityScore> abilityScores;
// private Set<String, Spell> spells;
// private Campaign campaign;
// private Map<String, String> notes;