package api;

import base.ApiBaseTest;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class UserSchemaTest extends ApiBaseTest {

    @Test
    public void testUserListSchema() {

        given()
                .spec(requestSpec)

                .when()
                .get("/users?page=2")

                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/user-list-schema.json"));
    }

    @Test
    public void testUserSchema() {

        given()
                .spec(requestSpec)

                .when()
                .get("/users/2")

                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    /**
     * 
     */
    @Test
    public void testCreateUserSchema() {

        String body = "{"
                + "\"name\":\"Huynh Kom\","
                + "\"job\":\"Tester\""
                + "}";

        given()
                .spec(requestSpec)
                .body(body)

                .when()
                .post("/users")

                .then()
                .statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/create-user-schema.json"));
    }
}