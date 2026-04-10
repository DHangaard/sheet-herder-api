package app.controllers.implementations;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
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
class SubraceControllerTest
{
    private static final int TEST_PORT = 7782;

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

    private long highElfId()
    {
        return seeded.get("highElf").getId();
    }

    @Nested
    @DisplayName("GET /subraces/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return 200 and correct subrace by id")
        void getById()
        {
            given()
                    .when()
                    .get("/subraces/" + highElfId())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) highElfId()))
                    .body("name", equalTo("High Elf"))
                    .body("race", notNullValue())
                    .body("race.name", equalTo("Elf"))
                    .body("traits", not(empty()));
        }

        @Test
        @DisplayName("Should return 404 when id does not exist")
        void getByIdNotFound()
        {
            given()
                    .when()
                    .get("/subraces/id/999999")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /subraces/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return 200 and correct subrace by name")
        void getByName()
        {
            given()
                    .when()
                    .get("/subraces/name/High Elf")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) highElfId()))
                    .body("name", equalTo("High Elf"))
                    .body("race", notNullValue())
                    .body("race.name", equalTo("Elf"))
                    .body("traits", not(empty()));
        }

        @Test
        @DisplayName("Should return 404 when name does not exist")
        void getByNameNotFound()
        {
            given()
                    .when()
                    .get("/subraces/name/NonExistentSubrace")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /subraces")
    class GetAll
    {
        @Test
        @DisplayName("Should return all subraces with status 200")
        void getAll()
        {
            given()
                    .when()
                    .get("/subraces")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("name", containsInAnyOrder("High Elf", "Wood Elf"));
        }

        @Test
        @DisplayName("Should return 200 and empty list when no subraces exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            given()
                    .when()
                    .get("/subraces")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}