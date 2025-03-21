package pl.wrona.webserver;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

public class ActuatorIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCheckIfHealthPathIsExposed() {
        given()
                .header(authHeader)
                .when()
                .get("/api/v1/actuator")
                .then()
                .statusCode(200)
                .assertThat()
                .body("_links.health.href", endsWith("/api/v1/actuator/health"))
                .body("_links.health.templated", equalTo(false));
    }

    @Test
    void shouldCheckIfHealthStatusIsUP() {
        given()
                .header(authHeader)
                .when()
                .get("/api/v1/actuator/health")
                .then()
                .statusCode(200)
                .assertThat()
                .body("status", equalTo("UP"));
    }
}
