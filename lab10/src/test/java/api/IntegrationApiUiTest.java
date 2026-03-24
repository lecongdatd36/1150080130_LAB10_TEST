package api;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import io.restassured.http.ContentType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class IntegrationApiUiTest {

    private WebDriver driver;
    private String token;
    private boolean loginPreconditionPassed;
    private boolean isApiAlive;

    private void setupDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
    }

    @BeforeMethod(alwaysRun = true)
    public void setupPerTest() {
        setupDriver();

        Map<String, String> body = new HashMap<>();
        body.put("email", "eve.holt@reqres.in");
        body.put("password", "cityslicka");

        Response response = given()
                .baseUri("https://reqres.in")
                .basePath("/api")
                .header("x-api-key", "reqres_1b853fa519f84b419fdc9fa195e81963")
                .header("Accept", "application/json")
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/login");

        loginPreconditionPassed = response.statusCode() == 200;

        if (loginPreconditionPassed) {
            token = response.jsonPath().getString("token");
            System.out.println("[API PRECONDITION] token = " + token);
        } else {
            token = null;
            System.out.println("[API PRECONDITION] login failed, status = " + response.statusCode());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownPerTest() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void verifyApiLoginPrecondition() {
        if (!loginPreconditionPassed) {
            throw new SkipException("Skip UI because API login precondition failed.");
        }
    }

    @Test(dependsOnMethods = "verifyApiLoginPrecondition")
    public void testUiLoginOnlyWhenPreconditionPasses() {
        // UI action: login bằng form nhập liệu (không inject token/session)
        loginByForm("standard_user", "secret_sauce");

        waitUntilInventoryPageLoaded();

        // Assertion: sau login URL phải chứa inventory
        Assert.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "URL must contain 'inventory' after login");

        // Assertion: title trang phải là Swag Labs
        Assert.assertEquals(driver.getTitle(), "Swag Labs", "Unexpected page title after login");
    }

    @Test
    public void testFullIntegrationFlow() {
        // API check: gọi GET /api/users để xác nhận API đang hoạt động
        Response apiCheck = given()
                .baseUri("https://reqres.in")
                .basePath("/api")
                .header("x-api-key", "reqres_1b853fa519f84b419fdc9fa195e81963")
                .header("Accept", "application/json")
                .when()
                .get("/users");

        isApiAlive = apiCheck.statusCode() == 200;
        if (!isApiAlive) {
            throw new SkipException("Skip UI flow because reqres API is not alive.");
        }

        // UI action: đăng nhập vào saucedemo
        loginByForm("standard_user", "secret_sauce");
        waitUntilInventoryPageLoaded();

        // UI action: thêm 2 sản phẩm vào giỏ
        driver.findElement(By.id("add-to-cart-sauce-labs-backpack")).click();
        driver.findElement(By.id("add-to-cart-sauce-labs-bike-light")).click();

        // Assertion: badge trên icon giỏ hàng phải bằng 2
        String badge = driver.findElement(By.cssSelector(".shopping_cart_badge")).getText();
        Assert.assertEquals(badge, "2", "Cart badge must be 2");

        // UI action: vào trang giỏ hàng
        driver.findElement(By.cssSelector(".shopping_cart_link")).click();

        // Assertion: trong giỏ phải có đúng 2 sản phẩm
        List<WebElement> cartItems = driver.findElements(By.cssSelector(".cart_item"));
        Assert.assertEquals(cartItems.size(), 2, "Cart must contain exactly 2 items");
    }

    private void loginByForm(String username, String password) {
        driver.get("https://www.saucedemo.com/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("login-button")));

        usernameInput.clear();
        usernameInput.sendKeys(username);

        passwordInput.clear();
        passwordInput.sendKeys(password);

        loginButton.click();
    }

    private void waitUntilInventoryPageLoaded() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("inventory"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-to-cart-sauce-labs-backpack")));
    }
}
