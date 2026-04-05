package app.controllers.implementations;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
import app.persistence.entities.IEntity;
import app.testutils.TestCleanDB;
import app.testutils.TestPopulator;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CharacterSheetControllerTest
{
    private static final int TEST_PORT = 7778;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT + "/api/v1";

    private EntityManagerFactory emf;
    private Javalin app;
    private Map<String, IEntity> seeded;

    private String johnToken;
    private String mortenToken;
    private String garyToken;

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
        TestPopulator populator = new TestPopulator(emf);
        populator.populate();
        seeded = populator.getSeededData();

        johnToken = login("john@test.com", "Password_1");
        mortenToken = login("morten@test.com", "Password_2");
        garyToken = login("gary@test.com", "Password_3");
    }

    @AfterAll
    void stopServer()
    {
        app.stop();
        emf.close();
    }

    private String login(String email, String password)
    {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("email", email, "password", password))
                .when()
                .post(BASE_URL + "/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private long johnSheet1Id()
    {
        return seeded.get("johnSheet1").getId();
    }

    @Nested
    @DisplayName("POST /character-sheets/")
    class Create
    {
        @Test
        @DisplayName("Should persist character sheet and return 201 with body")
        void create()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("name", "Gandalf"))
                    .when()
                    .post(BASE_URL + "/character-sheets/")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("name", equalTo("Gandalf"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void createNoToken()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Gandalf"))
                    .when()
                    .post(BASE_URL + "/character-sheets/")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 409 when name already exists for user")
        void createDuplicateName()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("name", "Aragorn"))
                    .when()
                    .post(BASE_URL + "/character-sheets/")
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void createBlankName()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("name", ""))
                    .when()
                    .post(BASE_URL + "/character-sheets/")
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("GET /character-sheets/{id}")
    class GetByID
    {
        @Test
        @DisplayName("Should return correct character sheet by id")
        void getById()
        {
            given()
                    .header("Authorization", "Bearer " + johnToken)
                    .when()
                    .get(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) johnSheet1Id()))
                    .body("name", equalTo("Aragorn"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void getByIdNoToken()
        {
            given()
                    .when()
                    .get(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 403 when user does not own the sheet")
        void getByIdForbidden()
        {
            given()
                    .header("Authorization", "Bearer " + mortenToken)
                    .when()
                    .get(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("Should return 404 when sheet does not exist")
        void getByIdNotFound()
        {
            given()
                    .header("Authorization", "Bearer " + johnToken)
                    .when()
                    .get(BASE_URL + "/character-sheets/999999")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PUT /character-sheets/{id}")
    class Update
    {
        @Test
        @DisplayName("Should persist updated name and return 200 with body")
        void update()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("name", "Strider"))
                    .when()
                    .put(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(200)
                    .body("id", equalTo((int) johnSheet1Id()))
                    .body("name", equalTo("Strider"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void updateNoToken()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("name", "Strider"))
                    .when()
                    .put(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 403 when user does not own the sheet")
        void updateForbidden()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + mortenToken)
                    .body(Map.of("name", "Strider"))
                    .when()
                    .put(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("Should return 400 when name is blank")
        void updateBlankName()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("name", ""))
                    .when()
                    .put(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("DELETE /character-sheets/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete sheet and return 204")
        void delete()
        {
            given()
                    .header("Authorization", "Bearer " + johnToken)
                    .when()
                    .delete(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void deleteNoToken()
        {
            given()
                    .when()
                    .delete(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 403 when user does not own the sheet")
        void deleteForbidden()
        {
            given()
                    .header("Authorization", "Bearer " + mortenToken)
                    .when()
                    .delete(BASE_URL + "/character-sheets/" + johnSheet1Id())
                    .then()
                    .statusCode(403);
        }
    }

    @Nested
    @DisplayName("GET /character-sheets")
    class GetAllByUser
    {
        @Test
        @DisplayName("Should return only sheets belonging to the authenticated user")
        void getAllByUser()
        {
            given()
                    .header("Authorization", "Bearer " + johnToken)
                    .when()
                    .get(BASE_URL + "/character-sheets")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(2))
                    .body("name", hasItems("Aragorn", "Legolas"));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void getAllByUserNoToken()
        {
            given()
                    .when()
                    .get(BASE_URL + "/character-sheets")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return empty list when user has no sheets")
        void getAllByUserEmpty()
        {
            given()
                    .header("Authorization", "Bearer " + garyToken)
                    .when()
                    .get(BASE_URL + "/character-sheets")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(0));
        }
    }
}