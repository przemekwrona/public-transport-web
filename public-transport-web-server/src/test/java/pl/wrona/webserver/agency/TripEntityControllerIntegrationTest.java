package pl.wrona.webserver.agency;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TrafficMode;
import org.igeolab.iot.pt.server.api.model.TripId2;
import org.igeolab.iot.pt.server.api.model.TripMode;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class TripEntityControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus201() {
        String routeCode = with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new Route()
                        .routeId(new RouteId()
                                .line("202")
                                .name("CHMIELNIK - PIERZCHNICA"))
                        .originStop(new Stop().id(142902L).name("Chmielnik"))
                        .destinationStop(new Stop().id(145543L).name("Kije"))
                        .active(true)
                        .google(false))
                .when()
                .post("/api/v1/agency/NEXTSTOP/routes")
                .then()
                .statusCode(200)
                .extract()
                .path("routeId.routeCode");

        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new CreateTripDetailsRequest()
                        .body(new TripsDetails()
                                .tripId(new TripId2()
                                        .routeId(new RouteId()
                                                .name("CHMIELNIK - PIERZCHNICA")
                                                .line("202"))
                                        .variantMode(TripMode.FRONT))
                                .isMainVariant(true)
                                .headsign("PIERZCHNICA")
                                .addTripProfilesItem(new TripProfile()
                                        .trafficMode(TrafficMode.NORMAL)
                                        .isDefault(true)
                                        .isCustomized(false)
                                        .customizedCommunicationVelocity(27)
                                        .addStopsItem(new StopTime()
                                                .stopId(142902L)
                                                .meters(0)
                                                .calculatedSeconds(0))
                                        .addStopsItem(new StopTime()
                                                .stopId(145543L)
                                                .meters(2)
                                                .calculatedSeconds(300)))))
                .when()
                .post("/api/v1/agency/NEXTSTOP/routes/" + routeCode + "/trips")
                .then()
                .statusCode(201)
                .assertThat()
                .body("status", equalTo("CREATED"));
    }

}
