package app.controllers.implementations;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthCheckControllerTest
{
    private static final int TEST_PORT = 7777;

    private EntityManagerFactory emf;
    private Javalin app;

    @BeforeAll
    void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1";
    }

    @AfterAll
    void stopServer()
    {
        app.stop();
        emf.close();
    }

    private void assertHealthStatus(int expectedStatus, String expectedBody)
    {
        given()
                .when()
                .get("/health-check")
                .then()
                .statusCode(expectedStatus)
                .body("status", equalTo(expectedBody));
    }

    private void restartServer()
    {
        app.stop();
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);
    }

    @Nested
    @DisplayName("GET /health-check")
    class HealthCheck
    {
        @Test
        @DisplayName("Should return 200 on healthy database connection")
        void healthCheck()
        {
            assertHealthStatus(200, "ok");
        }

        @Test
        @DisplayName("Should return 503 when database connection is unavailable")
        void healthCheckNoDBConnection()
        {
            emf.close();
            assertHealthStatus(503, "unavailable");
            restartServer();
        }
    }
}