package pl.wrona.webserver.agency;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class RouteEntityControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus2011() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new Route()
                        .routeId(new RouteId()
                                .line("201")
                                .name("KIELCE - BUSKO-ZDRÓJ"))
                        .originStop(new Stop()
                                .id(142902L)
                                .name("Chmielnik"))
                        .destinationStop(new Stop()
                                .id(145543L)
                                .name("Kije"))
                        .via("MORAWICA,CHMIELNIK")
                        .active(true)
                        .google(false))
                .when()
                .post("/api/v1/agency/NEXTSTOP/routes")
                .then()
                .statusCode(200)
                .assertThat()
                .body("status.status", equalTo("CREATED"));
    }

}
