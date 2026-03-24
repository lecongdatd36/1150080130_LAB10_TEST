package api;

import base.ApiBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertTrue;

public class PostApiTest extends ApiBaseTest {

    @Test
    public void getPostById_ShouldReturnCorrectData_AndPassSchema() {
        Response response = given()
                .spec(jsonSpec)
                .when()
                .get("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body(matchesJsonSchemaInClasspath("schemas/PostSchema.json"))
                .extract()
                .response();

        assertTrue(response.time() < 3000, "GET /posts/1 must respond under 3000ms");
    }

    @Test
    public void createPost_ShouldReturn201_AndValidPayload() {
        Response response = given()
                .spec(jsonSpec)
                .body("{\"userId\":1,\"title\":\"Hello\",\"body\":\"API Test\"}")
                .when()
                .post("/posts")
                .then()
                .statusCode(201)
                .body("userId", equalTo(1))
                .body("title", equalTo("Hello"))
                .body("body", equalTo("API Test"))
                .extract()
                .response();

        assertTrue(response.time() < 3000, "POST /posts must respond under 3000ms");
    }

    @Test
    public void updatePost_ShouldReturn200_AndUpdatedTitle() {
        Response response = given()
                .spec(jsonSpec)
                .body("{\"id\":1,\"userId\":1,\"title\":\"Updated\",\"body\":\"Updated body\"}")
                .when()
                .put("/posts/1")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("title", equalTo("Updated"))
                .extract()
                .response();

        assertTrue(response.time() < 3000, "PUT /posts/1 must respond under 3000ms");
    }

    @Test
    public void deletePost_ShouldReturn200() {
        Response response = given()
                .spec(jsonSpec)
                .when()
                .delete("/posts/1")
                .then()
                .statusCode(200)
                .extract()
                .response();

        assertTrue(response.time() < 3000, "DELETE /posts/1 must respond under 3000ms");
    }
}
