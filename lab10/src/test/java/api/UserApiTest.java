package api;

import base.ApiBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertTrue;

public class UserApiTest extends ApiBaseTest {

        @Test
        public void getUsers_ShouldReturn10Users() {
                Response response = given()
                                .spec(jsonSpec)
                                .when()
                                .get("/users")
                                .then()
                                .statusCode(200)
                                .body("", hasSize(10))
                                .extract()
                                .response();

                assertTrue(response.time() < 3000, "GET /users must respond under 3000ms");
        }

        @Test
        public void getUserById_ShouldMatchExpectedName_AndSchema() {
                Response response = given()
                                .spec(jsonSpec)
                                .when()
                                .get("/users/1")
                                .then()
                                .statusCode(200)
                                .body("id", equalTo(1))
                                .body("name", equalTo("Leanne Graham"))
                                .body(matchesJsonSchemaInClasspath("schemas/UserSchema.json"))
                                .extract()
                                .response();

                assertTrue(response.time() < 3000, "GET /users/1 must respond under 3000ms");
        }

        @Test
        public void getInvalidUser_ShouldReturn404() {
                Response response = given()
                                .spec(jsonSpec)
                                .when()
                                .get("/users/9999")
                                .then()
                                .statusCode(404)
                                .extract()
                                .response();

                assertTrue(response.time() < 3000, "GET /users/9999 must respond under 3000ms");
        }
}