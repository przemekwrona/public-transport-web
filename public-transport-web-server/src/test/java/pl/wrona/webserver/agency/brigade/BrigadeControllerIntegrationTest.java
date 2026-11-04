package pl.wrona.webserver.agency.brigade;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class BrigadeControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateBrigadeAndReturnStatus201() {
        String routeCode = with().contentType(ContentType.JSON)
                .header(authHeader)
                .body("""
                        {
                          "routeId": { "line": "201", "name": "KIELCE - BUSKO-ZDRÓJ" },
                          "originStop": { "id": 142902, "name": "Chmielnik" },
                          "destinationStop": { "id": 145543, "name": "Kije" },
                          "via": "MORAWICA,CHMIELNIK",
                          "active": true,
                          "google": false
                        }""")
                .when()
                .post("/api/v1/agency/NEXTSTOP/routes")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .path("routeId.routeCode");

        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body("""
                        {
                          "startDate": "2026-03-01",
                          "endDate": "2026-12-31"
                        }""")
                .when()
                .post("/api/v1/agency/NEXTSTOP/calendar-item")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body("""
                        {
                          "brigadeName": "DP/KIE-BUSKO/1",
                          "selectedRouteCode": "%s",
                          "calendarCode": "0001"
                        }""".formatted(routeCode))
                .when()
                .post("/api/v1/agency/NEXTSTOP/brigades")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("status.status", equalTo("CREATED"));
    }

    @Test
    void shouldGetBrigadeAndReturnStatus200() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .when()
                .get("/api/v1/agency/NEXTSTOP/brigades/0001")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

}
