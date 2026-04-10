package app.config.hibernate;

import java.util.Properties;

final class HibernateBaseProperties
{

    private HibernateBaseProperties()
    {
    }

    static Properties createBase()
    {
        Properties props = new Properties();
        // Connection / Session
        props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        props.put("hibernate.current_session_context_class", "thread");

        // Logging
        props.put("hibernate.show_sql", "false");
        props.put("hibernate.format_sql", "false");
        props.put("hibernate.use_sql_comments", "false");

        // HikariCP
        props.put("hibernate.hikari.maximumPoolSize", "10");
        props.put("hibernate.hikari.minimumIdle", "2");
        props.put("hibernate.hikari.connectionTimeout", "20000");

        // JDBC: Groups INSERT/UPDATE statements into batches to reduce DB round-trips.
        props.put("hibernate.jdbc.batch_size", "50");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        return props;
    }
}