package pl.wrona.webserver.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import pl.wrona.webserver.security.keycloak.KeycloakProperties;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
class KeycloakConfiguration {

    @Bean
    RestClient keycloakRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

}
