package pl.wrona.webserver.agency.route;

import io.restassured.http.ContentType;
import org.igeolab.iot.agency.api.model.CreateRoute;
import org.igeolab.iot.agency.api.model.CreateRouteStopTime;
import org.igeolab.iot.agency.api.model.CreateRouteTrip;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import java.math.BigDecimal;

import static io.restassured.RestAssured.with;
import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

class RouteControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus201() {
        with().contentType(ContentType.JSON)
                .body(new CreateRoute()
                        .line("201")
                        .addTripsItem(new CreateRouteTrip()
                                .addStopsItem(new CreateRouteStopTime().order(1).otpId(10032L))
                                .addStopsItem(new CreateRouteStopTime().order(2).otpId(10033L))
                                .addStopsItem(new CreateRouteStopTime().order(3)
                                        .name("Kielce")
                                        .lat(BigDecimal.valueOf(50.2210272))
                                        .lon(BigDecimal.valueOf(20.3074461)))))
                .when()
                .post("/api/v1/agency/NEXTBUS/routes")
                .then()
                .statusCode(201)
                .assertThat()
                .body("id", equalTo("NEXTBUS/201"));

        get("/api/v1/agency/NEXTBUS/routes/201")
                .then()
                .statusCode(200)
                .assertThat()
                .body("line", equalTo("201"));


    }

}