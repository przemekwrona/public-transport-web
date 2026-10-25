package pl.wrona.webserver.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class KeycloakAgencyProvisionerTest {

    private static final String ADMIN = "http://keycloak:8080/admin/realms/public-transport";

    @Test
    void createsTheAgencyGroupAndAssignsTheOwnerAndSuperUsers() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        expectToken(server);
        expectUser(server, "jan", "jan-id");
        expectUser(server, "pwrona", "pwrona-id");
        server.expect(requestTo(ADMIN + "/groups?search=agencies&exact=true"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer provisioning-token"))
                .andRespond(withSuccess("""
                        [{"id":"agencies-id","name":"agencies","path":"/agencies"}]
                        """, MediaType.APPLICATION_JSON));
        expectChild(server, "agencies-id", "NS", "agency-id");
        for (PreAgencyAuthorize.Role role : PreAgencyAuthorize.Role.values()) {
            expectChild(server, "agency-id", role.name(), "group-" + role.name());
        }
        expectJoin(server, "jan-id", "agency-id");
        expectJoin(server, "jan-id", "group-AGENCY_OWNER");
        expectJoin(server, "jan-id", "group-SUPER_USER");
        expectJoin(server, "pwrona-id", "agency-id");
        expectJoin(server, "pwrona-id", "group-SUPER_USER");

        provisioner(builder).createAgency("NS", "jan", Set.of("jan", "pwrona"));

        server.verify();
    }

    @Test
    void rejectsAnAgencyThatAlreadyExists() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        expectToken(server);
        expectUser(server, "jan", "jan-id");
        server.expect(requestTo(ADMIN + "/groups?search=agencies&exact=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        [{"id":"agencies-id","name":"agencies","path":"/agencies"}]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(ADMIN + "/groups/agencies-id/children"))
                .andExpect(method(POST))
                .andRespond(withStatus(HttpStatus.CONFLICT));

        assertThatThrownBy(() -> provisioner(builder).createAgency("NS", "jan", Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Agency already exists in Keycloak");
    }

    @Test
    void rejectsAnOwnerMissingFromKeycloak() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        expectToken(server);
        server.expect(requestTo(ADMIN + "/users?username=jan&exact=true"))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provisioner(builder).createAgency("NS", "jan", Set.of()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User jan does not exist in Keycloak");
    }

    private static void expectToken(MockRestServiceServer server) {
        server.expect(requestTo("http://keycloak:8080/realms/public-transport/protocol/openid-connect/token"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"access_token\":\"provisioning-token\"}", MediaType.APPLICATION_JSON));
    }

    private static void expectUser(MockRestServiceServer server, String username, String id) {
        server.expect(requestTo(ADMIN + "/users?username=" + username + "&exact=true"))
                .andExpect(method(GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer provisioning-token"))
                .andRespond(withSuccess("[{\"id\":\"" + id + "\",\"username\":\"" + username + "\"}]", MediaType.APPLICATION_JSON));
    }

    private static void expectChild(MockRestServiceServer server, String parentId, String name, String id) {
        server.expect(requestTo(ADMIN + "/groups/" + parentId + "/children"))
                .andExpect(method(POST))
                .andExpect(content().json("{\"name\":\"" + name + "\"}"))
                .andRespond(created(id));
    }

    private static void expectJoin(MockRestServiceServer server, String userId, String groupId) {
        server.expect(requestTo(ADMIN + "/users/" + userId + "/groups/" + groupId))
                .andExpect(method(PUT))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer provisioning-token"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
    }

    private static ResponseCreator created(String id) {
        return withStatus(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, ADMIN + "/groups/" + id);
    }

    private static KeycloakAgencyProvisioner provisioner(RestClient.Builder builder) {
        OAuth2ResourceServerProperties resourceServer = new OAuth2ResourceServerProperties();
        resourceServer.getJwt().setJwkSetUri("http://keycloak:8080/realms/public-transport/protocol/openid-connect/certs");
        return new KeycloakAgencyProvisioner(new KeycloakAdminApi(builder.build(), new KeycloakProperties(
                "public-transport-web",
                "public-transport-web-secret",
                "public-transport-provisioning",
                "public-transport-provisioning-secret"), resourceServer));
    }

}
