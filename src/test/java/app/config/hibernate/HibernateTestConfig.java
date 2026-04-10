package app.config.hibernate;

import jakarta.persistence.EntityManagerFactory;

import java.util.Properties;

public final class HibernateTestConfig
{

    private static volatile EntityManagerFactory emf;

    private HibernateTestConfig()
    {
    }

    public static EntityManagerFactory getEntityManagerFactory()
    {
        if (emf == null || !emf.isOpen())
        {
            synchronized (HibernateTestConfig.class)
            {
                if (emf == null || !emf.isOpen())
                {
                    emf = HibernateEmfBuilder.build(buildProps());
                }
            }
        }
        return emf;
    }

    private static Properties buildProps()
    {
        Properties props = HibernateBaseProperties.createBase();
        props.put("hibernate.connection.driver_class", "org.testcontainers.jdbc.ContainerDatabaseDriver");
        props.put("hibernate.connection.url", "jdbc:tc:postgresql:16.2:///test_db");

        props.put("hibernate.archive.autodetection", "hbm,class");
        props.put("hibernate.hbm2ddl.auto", "create-drop");

        return props;
    }
}