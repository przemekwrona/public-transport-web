package pl.wrona.webserver.security;

import io.restassured.http.ContentType;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteStopTime;
import org.igeolab.iot.agency.api.model.CreateRouteTrip;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import java.math.BigDecimal;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class AppUserControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateAppUserAccount() {
        with().contentType(ContentType.JSON)
                .body(new CreateAppUserRequest()
                        .username("pwrona")
                        .password("welcome!"))
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(202)
                .assertThat()
                .body("username", equalTo("pwrona"));
    }


}