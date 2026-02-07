package app.config;

import org.hibernate.cfg.Configuration;

final class EntityRegistry {

    private EntityRegistry() {}

    static void registerEntities(Configuration configuration) {
        // configuration.addAnnotatedClass(User.class);
        // TODO: Add more entities here...
    }
}