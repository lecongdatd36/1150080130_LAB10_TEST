package api;

import base.ApiBaseTest;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthTest extends ApiBaseTest {

    @DataProvider(name = "loginScenarios")
    public Object[][] loginScenarios() {

        return new Object[][] {

                // email, password, expectedStatus, expectedError

                { "eve.holt@reqres.in", "cityslicka", 200, null },

                { "eve.holt@reqres.in", "", 400, "Missing password" },

                { "", "cityslicka", 400, "Missing email or username" },

                { "notexist@reqres.in", "wrongpass", 400, "user not found" },

                { "invalid-email", "pass123", 400, "user not found" }
        };
    }

    @Test(dataProvider = "loginScenarios")
    public void testLoginScenarios(String email,
            String password,
            int expectedStatus,
            String expectedError) {

        Map<String, String> body = new HashMap<>();

        if (!email.isEmpty()) {
            body.put("email", email);
        }

        if (!password.isEmpty()) {
            body.put("password", password);
        }

        ValidatableResponse response =

                given()
                        .spec(requestSpec)
                        .body(body)

                        .when()
                        .post("/login")

                        .then()
                        .statusCode(expectedStatus);

        if (expectedError != null) {

            response.body("error",
                    containsString(expectedError));
        } else {
            response.body("token", not(isEmptyOrNullString()));
        }
    }

    @Test
    public void testRegisterSuccess() {

        Map<String, String> body = new HashMap<>();
        body.put("email", "eve.holt@reqres.in");
        body.put("password", "pistol");

        given()
                .spec(requestSpec)
                .body(body)

                .when()
                .post("/register")

                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("token", not(isEmptyOrNullString()));
    }

    @Test
    public void testRegisterMissingPassword() {

        Map<String, String> body = new HashMap<>();
        body.put("email", "sydney@fife");

        given()
                .spec(requestSpec)
                .body(body)

                .when()
                .post("/register")

                .then()
                .statusCode(400)
                .body("error", equalTo("Missing password"));
    }
}