package pl.wrona.webserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class WireMockConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockGeoapifyClient() {
        var wireMockConfiguration = new WireMockConfiguration()
                .port(8085)
                .usingFilesUnderDirectory("src/test/resources/wiremock/geoapify-client")
                .withRootDirectory("src/test/resources/wiremock/geoapify-client");

        return new WireMockServer(wireMockConfiguration);
    }
}
