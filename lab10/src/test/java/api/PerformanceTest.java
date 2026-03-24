package api;

import base.ApiBaseTest;

import io.qameta.allure.Step;

import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PerformanceTest extends ApiBaseTest {

    // ==============================
    // DATA PROVIDER SLA
    // ==============================

    @DataProvider(name = "slaData")
    public Object[][] slaData() {

        return new Object[][] {

                // method, endpoint, sla(ms)

                { "GET", "/users", 2000 },

                { "GET", "/users/2", 1500 },

                { "POST", "/users", 3000 },

                { "POST", "/login", 2000 },

                { "DELETE", "/users/2", 1000 }
        };
    }

    // ==============================
    // STEP ALLURE
    // ==============================

    @Step("Gọi {method} {endpoint} - SLA: {maxMs}ms")
    public ValidatableResponse callApi(String method, String endpoint, int maxMs) {

        Map<String, String> body = new HashMap<>();

        if (endpoint.contains("login")) {
            body.put("email", "eve.holt@reqres.in");
            body.put("password", "cityslicka");
        }

        if (endpoint.equals("/users") && method.equals("POST")) {
            body.put("name", "Huynh Kom");
            body.put("job", "Tester");
        }

        Response response =

                given()
                        .spec(requestSpec)
                        .body(body)

                        .when()
                        .request(method, endpoint);

        System.out.println("Response time: " + response.time());

        return response.then().time(lessThan((long) maxMs));
    }

    // ==============================
    // TEST SLA
    // ==============================

    @Test(dataProvider = "slaData")
    public void testSLA(String method, String endpoint, int maxMs) {

        ValidatableResponse response = callApi(method, endpoint, maxMs);

        if (endpoint.equals("/users") && method.equals("GET")) {

            response
                    .statusCode(200)
                    .body("data.size()", greaterThanOrEqualTo(1));
        }

        if (endpoint.equals("/users/2") && method.equals("GET")) {

            response
                    .statusCode(200)
                    .body("data.id", equalTo(2));
        }

        if (endpoint.equals("/users") && method.equals("POST")) {

            response
                    .statusCode(201)
                    .body("id", notNullValue());
        }

        if (endpoint.equals("/login")) {

            response
                    .statusCode(200)
                    .body("token", notNullValue());
        }

        if (endpoint.equals("/users/2") && method.equals("DELETE")) {

            response
                    .statusCode(204);
        }
    }

    // ==============================
    // MONITORING 10 LẦN
    // ==============================

    @Test
    public void monitoringUsersApi() {

        final String endpoint = "/users/2";
        final int runs = 10;

        long total = 0;
        long min = Long.MAX_VALUE;
        long max = 0;

        for (int i = 1; i <= runs; i++) {

            Response response =

                    given()
                            .spec(requestSpec)

                            .when()
                            .get(endpoint);

            response.then().statusCode(200);

            long time = response.time();

            total += time;

            if (time < min)
                min = time;

            if (time > max)
                max = time;

            System.out.println("Run " + i + " - " + endpoint + " = " + time + " ms");
        }

        double avg = (double) total / runs;

        System.out.println("============ SLA MONITORING ============");
        System.out.println("Endpoint: " + endpoint);
        System.out.println("Total calls: " + runs);
        System.out.println("Average: " + String.format("%.2f", avg) + " ms");
        System.out.println("Min: " + min + " ms");
        System.out.println("Max: " + max + " ms");
    }
}