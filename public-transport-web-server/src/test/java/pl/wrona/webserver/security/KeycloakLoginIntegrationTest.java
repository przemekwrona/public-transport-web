package pl.wrona.webserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import pl.wrona.webserver.BaseIntegrationTest;

import java.util.Base64;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SqlMergeMode(SqlMergeMode.MergeMode.OVERRIDE)
@Sql(statements = "SELECT 1", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class KeycloakLoginIntegrationTest extends BaseIntegrationTest {

    @Test
    void loginIssuesKeycloakAccessToken() {
        String token = authHeader.getValue().substring("Bearer ".length());
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        String header = decode(parts[0]);
        String payload = decode(parts[1]);
        assertThat(header).contains("\"alg\":\"RS256\"");
        assertThat(payload).contains("\"preferred_username\":\"pwrona\"");
        assertThat(payload).contains("/agencies/NEXTSTOP/owners");

        given()
                .when()
                .get("/api/v1/admin/users")
                .then()
                .statusCode(401);

        given()
                .header(authHeader)
                .when()
                .get("/api/v1/admin/users")
                .then()
                .statusCode(200);

        given()
                .header(authHeader)
                .when()
                .get("/api/v1/agency/NEXTSTOP")
                .then()
                .statusCode(200);

        given()
                .header(authHeader)
                .when()
                .get("/api/v1/agency/KOWAL")
                .then()
                .statusCode(403);
    }

    private static String decode(String value) {
        int padding = (4 - value.length() % 4) % 4;
        return new String(Base64.getUrlDecoder().decode(value + "=".repeat(padding)));
    }
}
