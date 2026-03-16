package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Campaign
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    // private User gameMaster;

    // @Column(unique = true)
    // private List<CharacterSheet> players;
    // private Map<String, String> gameMasterNotes; // TODO Maybe another Key?
    // private Map<String, String> sessionLogs; // TODO Maybe another Key?

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // public Campaign(User gameMaster) { this.gameMaster = gameMaster; }

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
