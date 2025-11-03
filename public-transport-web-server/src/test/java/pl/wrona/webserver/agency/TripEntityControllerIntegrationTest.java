package pl.wrona.webserver.agency;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class TripEntityControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus201() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new TripsDetails()
                        .tripId(new TripId().routeId(new RouteId()
                                .name("CHMIELNIK - PIERZCHNICA")
                                .line("202")))
                        .item(new Trip()
                                .headsign("PIERZCHNICA"))
                        .addStopsItem(new StopTime()
                                .stopId(10033L)
                                .meters(0)
                                .calculatedSeconds(0))
                        .addStopsItem(new StopTime()
                                .stopId(10032L)
                                .meters(2)
                                .calculatedSeconds(300)))
                .when()
                .post("/api/v1/trips")
                .then()
                .statusCode(201)
                .assertThat()
                .body("status", equalTo("CREATED"));
    }

}