package pl.wrona.webserver.agency.brigade;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class BrigadeControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void shouldCreateBrigadeAndReturnStatus202() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body("""
                        {
                            "brigadeNumber": "DP/KIE-BUSKO/1"
                        }""")
                .when()
                .post("/api/v1/brigades")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .assertThat()
                .body("status", equalTo("CREATED"));
    }

    @Test
    void shouldGetBrigadeAndReturnStatus200() {
        with().contentType(ContentType.JSON)
                .header(authHeader)
                .body("""
                        {
                            "brigadeNumber": "DP/KIE-BUSKO/1"
                        }""")
                .when()
                .get("/api/v1/brigades/details")
                .then()
                .statusCode(HttpStatus.OK.value());
//                .assertThat()
//                .body("brigadeNumber", equalTo("DP/KIE-BUSKO/1"));
    }


}