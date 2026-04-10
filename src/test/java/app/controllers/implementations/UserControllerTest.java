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
class UserControllerTest
{
    private static final int TEST_PORT = 7784;

    private EntityManagerFactory emf;
    private Javalin app;
    private Map<String, IEntity> seeded;

    private String johnToken;
    private String mortenToken;

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
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    private long johnId()
    {
        return seeded.get("john").getId();
    }

    private long mortenId()
    {
        return seeded.get("morten").getId();
    }

    @Nested
    @DisplayName("PUT /users/{id}")
    class Update
    {
        @Test
        @DisplayName("Should update username and return 200 with body")
        void updateUsername()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("username", "john_updated"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(200)
                    .body("username", equalTo("john_updated"));
        }

        @Test
        @DisplayName("Should update email and return 200 with body")
        void updateEmail()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("email", "john_updated@test.com"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(200)
                    .body("email", equalTo("john_updated@test.com"));
        }

        @Test
        @DisplayName("Should update password, return 200 and not expose hashed password in response body")
        void updatePasswordNotExposed()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("password", "Updated_Password1!"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(200)
                    .body("$", not(hasKey("password")))
                    .body("$", not(hasKey("hashedPassword")));
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void updateNoToken()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("username", "john_updated"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 403 when user does not own the account")
        void updateForbidden()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + mortenToken)
                    .body(Map.of("username", "john_updated"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(403);
        }

        @Test
        @DisplayName("Should return 409 when username is unavailable")
        void updateConflictUsername()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("username", "morten"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Should return 409 when email is unavailable")
        void updateConflictEmail()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of("email", "morten@test.com"))
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Should return 200 when body has no fields to update")
        void updateNoFields()
        {
            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + johnToken)
                    .body(Map.of())
                    .when()
                    .put("/users/" + johnId())
                    .then()
                    .statusCode(200)
                    .body("email", equalTo("john@test.com"))
                    .body("username", equalTo("john"));
        }
    }

    @Nested
    @DisplayName("DELETE /users/{id}")
    class Delete
    {
        @Test
        @DisplayName("Should delete account and return 204")
        void delete()
        {
            given()
                    .header("Authorization", "Bearer " + johnToken)
                    .when()
                    .delete("/users/" + johnId())
                    .then()
                    .statusCode(204);
        }

        @Test
        @DisplayName("Should return 401 when no token is provided")
        void deleteNoToken()
        {
            given()
                    .when()
                    .delete("/users/" + johnId())
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 403 when user does not own the account")
        void deleteForbidden()
        {
            given()
                    .header("Authorization", "Bearer " + mortenToken)
                    .when()
                    .delete("/users/" + johnId())
                    .then()
                    .statusCode(403);
        }
    }
}