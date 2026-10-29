package pl.wrona.webserver.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import pl.wrona.webserver.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;

class KeycloakUserProvisionerTest {

    @Test
    void createsTheUserWithTheProvisioningClient() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andExpect(method(POST))
                .andExpect(content().string(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("grant_type=client_credentials"),
                        org.hamcrest.Matchers.containsString("client_id=public-transport-provisioning"),
                        org.hamcrest.Matchers.containsString("client_secret=public-transport-provisioning-secret"))))
                .andRespond(withSuccess("{\"access_token\":\"provisioning-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://keycloak:8080/admin/realms/public-transport/users"))
                .andExpect(method(POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer provisioning-token"))
                .andExpect(content().json("""
                        {
                          "username": "jan",
                          "email": "jan@example.com",
                          "enabled": true,
                          "emailVerified": true,
                          "credentials": [
                            {"type": "password", "value": "welcome1", "temporary": false}
                          ]
                        }
                        """))
                .andRespond(withStatus(HttpStatus.CREATED));

        provisioner(builder).createUser("jan", "jan@example.com", "welcome1");

        server.verify();
    }

    @Test
    void rejectsAUserThatAlreadyExists() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andRespond(withSuccess("{\"access_token\":\"provisioning-token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://keycloak:8080/admin/realms/public-transport/users"))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> provisioner(builder).createUser("jan", "jan@example.com", "welcome1"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User already exists in Keycloak");
    }

    @Test
    void rejectsAProvisioningClientThatCannotLogIn() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andRespond(withUnauthorizedRequest());

        assertThatThrownBy(() -> provisioner(builder).createUser("jan", "jan@example.com", "welcome1"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Keycloak provisioning login failed with HTTP 401");
    }

    private static KeycloakUserProvisioner provisioner(RestClient.Builder builder) {
        OAuth2ResourceServerProperties resourceServer = new OAuth2ResourceServerProperties();
        resourceServer.getJwt().setJwkSetUri("http://keycloak:8080/realms/public-transport/protocol/openid-connect/certs");
        return new KeycloakUserProvisioner(new KeycloakAdminApi(builder.build(), new KeycloakProperties(
                "public-transport-web",
                "public-transport-web-secret",
                "public-transport-provisioning",
                "public-transport-provisioning-secret"), resourceServer));
    }

}
