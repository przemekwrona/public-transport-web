package pl.wrona.webserver.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class KeycloakAdminApiTest {

    @Test
    void logsInWithTheProvisioningClient() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andExpect(method(POST))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=client_credentials"),
                        org.hamcrest.Matchers.containsString("client_id=public-transport-provisioning"),
                        org.hamcrest.Matchers.containsString("client_secret=public-transport-provisioning-secret"))))
                .andRespond(withSuccess("{\"access_token\":\"provisioning-token\"}", MediaType.APPLICATION_JSON));

        String token = api(builder).accessToken();

        assertThat(token).isEqualTo("provisioning-token");
        server.verify();
    }

    @Test
    void buildsTheAdminUrlFromTheJwkSetUri() {
        RestClient.Builder builder = RestClient.builder();

        String admin = api(builder).admin("/users");

        assertThat(admin).isEqualTo("http://keycloak:8080/admin/realms/public-transport/users");
    }

    @Test
    void rejectsAProvisioningClientThatCannotLogIn() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> api(builder).accessToken())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Keycloak provisioning login failed with HTTP 401");
    }

    private static KeycloakAdminApi api(RestClient.Builder builder) {
        OAuth2ResourceServerProperties resourceServer = new OAuth2ResourceServerProperties();
        resourceServer.getJwt().setJwkSetUri("http://keycloak:8080/realms/public-transport/protocol/openid-connect/certs");
        return new KeycloakAdminApi(builder.build(), new KeycloakProperties(
                "public-transport-web",
                "public-transport-web-secret",
                "public-transport-provisioning",
                "public-transport-provisioning-secret"), resourceServer);
    }

}
