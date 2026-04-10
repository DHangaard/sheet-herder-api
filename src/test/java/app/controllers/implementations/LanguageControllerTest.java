package app.controllers.implementations;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
import app.enums.LanguageType;
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
class LanguageControllerTest
{
    private static final int TEST_PORT = 7780;

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

    private long elvishId()
    {
        return seeded.get("elvish").getId();
    }

    @Nested
    @DisplayName("GET /languages/{id}")
    class GetById
    {
        @Test
        @DisplayName("Should return 200 and correct language by id")
        void getById()
        {
            given()
                    .when()
                    .get("/languages/" + elvishId())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) elvishId()))
                    .body("name", equalTo("Elvish"))
                    .body("type", equalTo(LanguageType.STANDARD.getValue()))
                    .body("typicalSpeakers", hasItem("Elves"))
                    .body("script", equalTo("Elvish"));
        }

        @Test
        @DisplayName("Should return 404 when id does not exist")
        void getByIdNotFound()
        {
            given()
                    .when()
                    .get("/languages/999999")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /languages/name/{name}")
    class GetByName
    {
        @Test
        @DisplayName("Should return 200 and correct language by name")
        void getByName()
        {
            given()
                    .when()
                    .get("/languages/name/Elvish")
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) elvishId()))
                    .body("name", equalTo("Elvish"))
                    .body("type", equalTo(LanguageType.STANDARD.getValue()))
                    .body("typicalSpeakers", hasItem("Elves"))
                    .body("script", equalTo("Elvish"));
        }

        @Test
        @DisplayName("Should return 404 when name does not exist")
        void getByNameNotFound()
        {
            given()
                    .when()
                    .get("/languages/name/NonExistentLanguage")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /languages")
    class GetAll
    {
        @Test
        @DisplayName("Should return all languages with status 200")
        void getAll()
        {
            given()
                    .when()
                    .get("/languages")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(3))
                    .body("name", containsInAnyOrder("Elvish", "Common", "Sylvan"));
        }

        @Test
        @DisplayName("Should return 200 and empty list when no languages exist")
        void getAllEmpty()
        {
            TestCleanDB.truncateTables(emf);

            given()
                    .when()
                    .get("/languages")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}