package pl.wrona.webserver.agency;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class TripEntityControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus201() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new Trip()
                        .name("CHMIELNIK - PIERZCHNICA")
                        .line("202")
                        .headsign("PIERZCHNICA")
                        .addStopsItem(new StopTime()
                                .stopId("10033")
                                .meters(0.0F)
                                .minutes(0.0F))
                        .addStopsItem(new StopTime()
                                .stopId("10032")
                                .meters(2.0F)
                                .minutes(5.0F)))
                .when()
                .post("/api/v1/trips")
                .then()
                .statusCode(201)
                .assertThat()
                .body("status", equalTo("CREATED"));
    }

}