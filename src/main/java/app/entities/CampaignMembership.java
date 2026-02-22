package app.entities;

import app.enums.CampaignRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Entity
public class CampaignMembership
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Long userId; // TODO should this be User?

    @Column(nullable = false)
    private Long campaignId; // TODO should this be Campaign?

    @Enumerated(EnumType.STRING)
    private CampaignRole role;
}
