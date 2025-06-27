package pl.wrona.webserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.wrona.webserver.security.AppUserService;

@ActiveProfiles("test")
@Testcontainers
@Sql({"/sql/init.sql"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { WireMockConfig.class })
public class BaseIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    @LocalServerPort
    private Integer port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    protected WireMockServer mockGeoapifyClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AppUserService appUserService;

    protected Header authHeader;

    @BeforeAll
    static void setup() {
        if (!postgres.isRunning()) {
            postgres.start();
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;

        LoginAppUserResponse loginResponse = appUserService.login(new LoginAppUserRequest().username("pwrona").password("welcome1"));
        authHeader = new Header("Authorization", "Bearer %s".formatted(loginResponse.getToken()));
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

    }

    @Override
    public void close() throws Throwable {
        if (postgres.isRunning()) {
            postgres.stop();
        }
    }
}
