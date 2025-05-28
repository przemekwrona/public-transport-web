package pl.wrona.webserver.security;

import io.restassured.http.ContentType;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import pl.wrona.webserver.BaseIntegrationTest;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.equalTo;

class AppUserControllerIntegrationTest extends BaseIntegrationTest {

//    @Disabled
//    @Test
//    void shouldCreateAppUserAccount() {
//        with().contentType(ContentType.JSON)
//                .body(new CreateAppUserRequest()
//                        .username("pwrona")
//                        .password("welcome!")
//                        .email("pwrona@igeolab.pl"))
//                .when()
//                .post("/api/v1/users")
//                .then()
//                .statusCode(201)
//                .assertThat()
//                .body("username", equalTo("pwrona"));
//    }


}