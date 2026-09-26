package pl.wrona.webserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import pl.wrona.webserver.security.AppUserService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@ActiveProfiles("test")
@Testcontainers
@Sql(value = {"/sql/init.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = { WireMockConfig.class })
public class BaseIntegrationTest implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    @LocalServerPort
    private Integer port;

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
            .withDatabaseName("next_stop_db")
            .withUsername("test")
            .withPassword("welcome1");

    @SuppressWarnings("resource")
    public static final GenericContainer<?> keycloak = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:26.7.4"))
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "admin")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "admin")
            .withEnv("KC_HTTP_ENABLED", "true")
            .withCopyToContainer(Transferable.of(readRealmImport()), "/opt/keycloak/data/import/public-transport-realm.json")
            .withCommand("start-dev", "--import-realm")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/realms/public-transport").forPort(8080).forStatusCode(200).withStartupTimeout(Duration.ofMinutes(3)));

    static {
        keycloak.start();
    }

    @Autowired
    protected WireMockServer mockGeoapifyClient;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("keycloak.enabled", () -> "true");
        registry.add("keycloak.server-url", BaseIntegrationTest::keycloakUrl);
        registry.add("keycloak.issuer-uri", () -> keycloakUrl() + "/realms/public-transport");
        registry.add("keycloak.jwk-set-uri", () -> keycloakUrl() + "/realms/public-transport/protocol/openid-connect/certs");
    }

    private static String keycloakUrl() {
        return "http://localhost:" + keycloak.getMappedPort(8080);
    }

    private static byte[] readRealmImport() {
        Path cwd = Path.of("").toAbsolutePath();
        Path inModule = cwd.resolve("../keycloak/import/public-transport-realm.json").normalize();
        Path inRepo = cwd.resolve("keycloak/import/public-transport-realm.json").normalize();
        Path realm = Files.exists(inModule) ? inModule : inRepo;
        try {
            return Files.readAllBytes(realm);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot read Keycloak realm import from " + realm, exception);
        }
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
