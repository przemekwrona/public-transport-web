package pl.wrona.webserver.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import static org.springframework.http.HttpMethod.POST;

class KeycloakLoginClientTest {

    @Test
    void returnsTheAccessTokenFromKeycloak() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andExpect(method(POST))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=password"),
                        org.hamcrest.Matchers.containsString("username=pwrona"))))
                .andRespond(withSuccess("{\"access_token\":\"keycloak-token\"}", MediaType.APPLICATION_JSON));

        String token = client(builder).login("pwrona", "welcome1");

        assertThat(token).isEqualTo("keycloak-token");
        server.verify();
    }

    @Test
    void rejectsInvalidCredentials() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> client(builder).login("pwrona", "wrong"))
                .isInstanceOf(BadCredentialsException.class);
    }

    private static KeycloakLoginClient client(RestClient.Builder builder) {
        OAuth2ResourceServerProperties resourceServer = new OAuth2ResourceServerProperties();
        resourceServer.getJwt().setIssuerUri("http://localhost:8081/realms/public-transport");
        resourceServer.getJwt().setJwkSetUri("http://keycloak:8080/realms/public-transport/protocol/openid-connect/certs");
        return new KeycloakLoginClient(builder.build(), new KeycloakProperties(
                "public-transport-web",
                "public-transport-web-secret",
                "public-transport-provisioning",
                "public-transport-provisioning-secret"), resourceServer);
    }

}
