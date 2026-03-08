package app;

import app.integrations.DNDClient;
import app.config.HibernateConfig;
import app.utils.ExecutionTimer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;

import java.net.http.HttpClient;

public class Main
{
    // Singleton
    private final static EntityManagerFactory ENTITY_MANAGER_FACTORY = HibernateConfig.getEntityManagerFactory();
    private final static HttpClient CLIENT = HttpClient.newHttpClient();
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void main(String[] args)
    {
        ExecutionTimer.start();

        DNDClient dndClient = new DNDClient(CLIENT, OBJECT_MAPPER);
        dndClient.getAllRaces().races().forEach(race -> System.out.println(race.name()));

        ENTITY_MANAGER_FACTORY.close();

        ExecutionTimer.finish();
    }
}