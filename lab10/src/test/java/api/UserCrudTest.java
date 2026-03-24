package api;

import base.ApiBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

public class UserCrudTest extends ApiBaseTest {

    private String userId;

    @Test(priority = 1)
    public void createUser() {

        Map<String, String> body = new HashMap<>();
        body.put("name", "Huynh Kom");
        body.put("job", "Tester");

        Response response =

                given()
                        .spec(requestSpec)
                        .body(body)

                        .when()
                        .post("/users");

        response.then()
                .statusCode(201)
                .body("name", equalTo("Huynh Kom"))
                .body("job", equalTo("Tester"))
                .body("id", notNullValue());

        userId = response.jsonPath().getString("id");
    }

    @Test(priority = 2)
    public void getUser() {

        given()
                .spec(requestSpec)

                .when()
                .get("/users/2")

                .then()
                .statusCode(200)
                .body("data.id", equalTo(2));
    }

    @Test(priority = 3)
    public void updateUserPut() {

        Map<String, String> body = new HashMap<>();
        body.put("name", "Huynh Kom");
        body.put("job", "Senior Tester");

        given()
                .spec(requestSpec)
                .body(body)

                .when()
                .put("/users/2")

                .then()
                .statusCode(200)
                .body("job", equalTo("Senior Tester"));
    }

    @Test(priority = 4)
    public void updateUserPatch() {

        Map<String, String> body = new HashMap<>();
        body.put("job", "Lead Tester");

        given()
                .spec(requestSpec)
                .body(body)

                .when()
                .patch("/users/2")

                .then()
                .statusCode(200)
                .body("job", equalTo("Lead Tester"));
    }

    @Test(priority = 5)
    public void deleteUser() {

        given()
                .spec(requestSpec)

                .when()
                .delete("/users/2")

                .then()
                .statusCode(204);
    }
}