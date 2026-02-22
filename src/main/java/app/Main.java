package app;

import app.config.HibernateConfig;
import jakarta.persistence.EntityManagerFactory;

public class Main
{
    // Singleton
    private final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    public static void main(String[] args)
    {
        System.out.println("hello, world");

        emf.close();
    }
}