package pl.wrona.webserver.security;

import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import pl.wrona.webserver.security.keycloak.KeycloakLoginClient;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private final KeycloakLoginClient keycloakLoginClient = mock(KeycloakLoginClient.class);
    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final AppUserService appUserService = new AppUserService(keycloakLoginClient, jwtDecoder);

    @Test
    void loginReadsRolesAndAgencyFromTheToken() {
        Jwt jwt = Jwt.withTokenValue("keycloak-token")
                .header("alg", "RS256")
                .subject("pwrona")
                .issuedAt(Instant.parse("2026-10-23T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-23T01:00:00Z"))
                .claim("preferred_username", "pwrona")
                .claim("groups", List.of("/agencies/NEXTSTOP/AGENCY_OWNER", "/agencies/NEXTSTOP/SUPER_USER"))
                .build();
        when(keycloakLoginClient.login("pwrona", "welcome1")).thenReturn("keycloak-token");
        when(jwtDecoder.decode("keycloak-token")).thenReturn(jwt);

        LoginAppUserResponse response = appUserService.login(new LoginAppUserRequest().username("pwrona").password("welcome1"));

        assertThat(response.getToken()).isEqualTo("keycloak-token");
        assertThat(response.getInstance()).isEqualTo("NEXTSTOP");
        assertThat(response.getRoles()).containsExactly("AGENCY_OWNER", "SUPER_USER");
    }

}
