package app.config;

import app.persistence.entities.domain.Campaign;
import app.persistence.entities.domain.CampaignMembership;
import app.persistence.entities.domain.CharacterSheet;
import app.persistence.entities.domain.User;
import app.persistence.entities.reference.Language;
import app.persistence.entities.reference.Race;
import app.persistence.entities.reference.Subrace;
import app.persistence.entities.reference.Trait;
import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(CharacterSheet.class);
        configuration.addAnnotatedClass(Campaign.class);
        configuration.addAnnotatedClass(CampaignMembership.class);
        configuration.addAnnotatedClass(Race.class);
        configuration.addAnnotatedClass(Subrace.class);
        configuration.addAnnotatedClass(Language.class);
        configuration.addAnnotatedClass(Trait.class);
    }
}