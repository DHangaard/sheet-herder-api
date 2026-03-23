package app.persistence.entities.reference;

import app.enums.LanguageType;
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
public class Language
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private LanguageType type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "language_typical_speakers",
            joinColumns = @JoinColumn(name = "language_id")
    )
    @Column(name = "typical_speaker")
    private List<String> typicalSpeakers;

    private String script;

    @Column(nullable = false, length = 64)
    private String contentHash;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Language(String name, String description, LanguageType type, List<String> typicalSpeakers, String script, String contentHash)
    {
        this.name = name;
        this.description = description;
        this.type = type;
        this.typicalSpeakers = typicalSpeakers;
        this.script = script;
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
