package app.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
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

    @Column(nullable = false, unique = true)
    private String name;

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

    private LocalDate createdAt;
    private LocalDate updatedAt;

    // Implement Character Sheet from Dungeons & Dragons 5th Edition (2024)

    @PrePersist
    protected void onCreate()
    {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.name.trim();
    }

    @PreUpdate
    protected void onUpdate()
    {
        this.updatedAt = LocalDate.now();
        this.name.trim();
    }

}
