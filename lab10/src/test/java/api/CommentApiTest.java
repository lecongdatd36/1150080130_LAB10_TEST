package api;

import base.ApiBaseTest;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.assertTrue;

public class CommentApiTest extends ApiBaseTest {

    @Test
    public void getCommentsByPostId_ShouldReturnFiveComments() {
        Response response = given()
                .spec(jsonSpec)
                .when()
                .get("/posts/1/comments")
                .then()
                .statusCode(200)
                .body("", hasSize(5))
                .body("[0].postId", equalTo(1))
                .extract()
                .response();

        assertTrue(response.time() < 3000, "GET /posts/1/comments must respond under 3000ms");
    }
}
