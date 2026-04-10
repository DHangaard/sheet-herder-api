package app.controllers.implementations;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
import app.enums.Size;
import app.persistence.entities.IEntity;
import app.testutils.ReferenceDataPopulator;
import app.testutils.TestCleanDB;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RaceControllerTest
{
    private static final int TEST_PORT = 7781;

    private EntityManagerFactory emf;
    private Javalin app;
    private java.util.Map<String, IEntity> seeded;

    @BeforeAll
    void startServer()
    {
        emf = HibernateTestConfig.getEntityManagerFactory();
        app = ApplicationConfig.startServer(TEST_PORT, emf);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = TEST_PORT;
        RestAssured.basePath = "/api/v1";
    }

    @BeforeEach
    void reset()
    {
        TestCleanDB.truncateTables(emf);
        ReferenceDataPopulator populator = new ReferenceDataPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();
    }

    @AfterAll
    void stopServer()
    {
        app.stop();
        emf.close();
    }

    private long elfId()
    {
        return seeded.get("elf").getId();
    }

    @Nested
    @DisplayName("GET /races/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return 200 and correct race by id")
        void getById()
        {
            given()
                    .when()
                    .get("/races/" + elfId())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) elfId()))
                    .body("name", equalTo("Elf"))
                    .body("speed", equalTo(30))
                    .body("size", equalTo(Size.MEDIUM.getValue()))
                    .body("languages", not(empty()))
                    .body("traits", not(empty()));
        }

        @Test
        @DisplayName("Should return 404 when id does not exist")
        void getByIdNotFound()
        {
            given()
                    .when()
                    .get("/races/999999")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /races/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return 200 and correct race by name")
        void getByName()
        {
            given()
                    .when()
                    .get("/races/name/Elf")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) elfId()))
                    .body("name", equalTo("Elf"))
                    .body("speed", equalTo(30))
                    .body("size", equalTo(Size.MEDIUM.getValue()))
                    .body("languages", not(empty()))
                    .body("traits", not(empty()));
        }

        @Test
        @DisplayName("Should return 404 when name does not exist")
        void getByNameNotFound()
        {
            given()
                    .when()
                    .get("/races/name/NonExistentRace")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /races")
    class GetAll
    {
        @Test
        @DisplayName("Should return all races with status 200")
        void getAll()
        {
            given()
                    .when()
                    .get("/races")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("name", containsInAnyOrder("Elf", "Human"));
        }

        @Test
        @DisplayName("Should return 200 and empty list when no races exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            given()
                    .when()
                    .get("/races")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}