package app.security.controllers;

import app.config.ApplicationConfig;
import app.config.hibernate.HibernateTestConfig;
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
class SecurityControllerTest
{
    private static final int TEST_PORT = 7778;

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

    @BeforeEach
    void setUp()
    {
        TestCleanDB.truncateTables(emf);
        new TestPopulator(emf).populate();
    }

    @AfterAll
    void stopServer()
    {
        app.stop();
        emf.close();
    }

    @Nested
    @DisplayName("POST /auth/register")
    class Register
    {
        @Test
        @DisplayName("Should create user and return id and username")
        void register()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "new@test.com", "username", "new_user", "password", "Password_99"))
                    .when()
                    .post("/auth/register")
                    .then()
                    .statusCode(201)
                    .body("id", notNullValue())
                    .body("username", equalTo("new_user"));
        }

        @Test
        @DisplayName("Should return 409 when email is already taken")
        void registerDuplicateEmail()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "john@test.com", "username", "other_john", "password", "Password_99"))
                    .when()
                    .post("/auth/register")
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Should return 409 when username is already taken")
        void registerDuplicateUsername()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "fake-john@test.com", "username", "john", "password", "Password_99"))
                    .when()
                    .post("/auth/register")
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("POST /auth/login")
    class Login
    {
        @Test
        @DisplayName("Should return token on valid credentials")
        void login()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "john@test.com", "password", "Password_1"))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(200)
                    .body("token", notNullValue());
        }

        @Test
        @DisplayName("Should return 401 on wrong password")
        void loginWrongPassword()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "john@test.com", "password", "wrongpassword"))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(401);
        }

        @Test
        @DisplayName("Should return 401 on unknown email")
        void loginUnknownEmail()
        {
            given()
                    .contentType(ContentType.JSON)
                    .body(Map.of("email", "nobody@test.com", "password", "Password_1"))
                    .when()
                    .post("/auth/login")
                    .then()
                    .statusCode(401);
        }
    }
}