package pl.wrona.webserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.wrona.webserver.security.AppUserService;

@ActiveProfiles("test")
@Sql(value = {"/sql/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { WireMockConfig.class })
public class BaseIntegrationTest {

    @LocalServerPort
    private Integer port;

    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("next_stop_db")
            .withUsername("test")
            .withPassword("welcome1")
            .withUrlParam("currentSchema", "public_transport")
            .withInitScript("sql/create-schema.sql");

    static {
        postgres.start();
    }

    @Autowired
    protected WireMockServer mockGeoapifyClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", WireMockConfig::keycloakIssuer);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> WireMockConfig.keycloakIssuer() + "/protocol/openid-connect/certs");
    }

    @Autowired
    private AppUserService appUserService;

    protected Header authHeader;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        LoginAppUserResponse loginResponse = appUserService.login(new LoginAppUserRequest().username("pwrona").password("welcome1"));
        authHeader = new Header("Authorization", "Bearer %s".formatted(loginResponse.getToken()));
    }
}
