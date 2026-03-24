package base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.specification.RequestSpecification;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import org.testng.annotations.BeforeClass;
import org.testng.SkipException;

import io.restassured.RestAssured;

public class ApiBaseTest {

    protected RequestSpecification requestSpec;
    protected RequestSpecification jsonSpec;

    @BeforeClass
    public void setup() {
        String apiKey = System.getenv("REQRES_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getProperty("reqres.apiKey");
        }

        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = "reqres_1b853fa519f84b419fdc9fa195e81963";
        }

        RestAssured.useRelaxedHTTPSValidation();

        Filter skipOnRateLimitFilter = (requestSpec, responseSpec, ctx) -> {
            Response response = ctx.next(requestSpec, responseSpec);
            if (response.getStatusCode() == 429) {
                String reset = response.getHeader("Ratelimit-Reset");
                throw new SkipException("ReqRes rate limit reached (429). Retry after "
                        + (reset != null ? reset + "s" : "some time") + ".");
            }
            return response;
        };

        requestSpec = new RequestSpecBuilder()
                .setBaseUri("https://reqres.in")
                .setBasePath("/api")
                .setContentType(ContentType.JSON)

                .addHeader("x-api-key", apiKey)

                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "Mozilla/5.0")

                .addFilter(skipOnRateLimitFilter)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())

                .build();

        jsonSpec = new RequestSpecBuilder()
                .setBaseUri("https://jsonplaceholder.typicode.com")
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }
}