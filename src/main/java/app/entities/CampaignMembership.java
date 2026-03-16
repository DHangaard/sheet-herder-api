package app.entities;

import app.enums.CampaignRole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_user_membership_campaign", columnNames = {"user_id", "campaign_id"}))
public class CampaignMembership
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @OneToOne(optional = true)
    @JoinColumn(name = "character_sheet_id", unique = true)
    private CharacterSheet characterSheet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignRole role;
}
