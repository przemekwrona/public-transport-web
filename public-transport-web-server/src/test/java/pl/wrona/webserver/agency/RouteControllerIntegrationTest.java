package pl.wrona.webserver.agency;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.Route;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class RouteControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateRouteAndReturnStatus2011() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body(new Route()
                        .name("KIELCE - BUSKO-ZDRÓJ")
                        .line("201")
                        .origin("KIELCE")
                        .destination("BUSKO-ZDRÓJ")
                        .via("MORAWICA,CHMIELNIK"))
                .when()
                .post("/api/v1/routes")
                .then()
                .statusCode(202)
                .assertThat()
                .body("status", equalTo("CREATED"));
    }

}