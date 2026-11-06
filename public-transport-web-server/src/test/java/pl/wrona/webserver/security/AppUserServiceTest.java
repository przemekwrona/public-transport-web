package pl.wrona.webserver.security;

import org.igeolab.iot.pt.server.api.model.ChangePasswordRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.keycloak.KeycloakLoginClient;
import pl.wrona.webserver.security.keycloak.KeycloakUserProvisioner;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppUserServiceTest {

    private final KeycloakLoginClient keycloakLoginClient = mock(KeycloakLoginClient.class);
    private final JwtDecoder jwtDecoder = mock(JwtDecoder.class);
    private final KeycloakUserProvisioner keycloakUserProvisioner = mock(KeycloakUserProvisioner.class);
    private final AppUserService appUserService = new AppUserService(keycloakLoginClient, jwtDecoder, keycloakUserProvisioner);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

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

    @Test
    void changePasswordUpdatesKeycloakAfterTheCurrentPasswordMatches() {
        authenticate("pwrona");
        when(keycloakLoginClient.login("pwrona", "welcome1")).thenReturn("fresh-token");

        Status status = appUserService.changePassword(new ChangePasswordRequest()
                .currentPassword("welcome1")
                .newPassword("welcome2"));

        assertThat(status.getStatus()).isEqualTo(Status.StatusEnum.SUCCESS);
        verify(keycloakUserProvisioner).changePassword("pwrona", "welcome2");
    }

    @Test
    void changePasswordRejectsAnInvalidCurrentPassword() {
        authenticate("pwrona");
        when(keycloakLoginClient.login("pwrona", "wrong")).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> appUserService.changePassword(new ChangePasswordRequest()
                .currentPassword("wrong")
                .newPassword("welcome2")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", "ERROR:202611060001");
        verify(keycloakUserProvisioner, never()).changePassword("pwrona", "welcome2");
    }

    @Test
    void changePasswordRejectsANewPasswordEqualToTheCurrentOne() {
        authenticate("pwrona");
        when(keycloakLoginClient.login("pwrona", "welcome1")).thenReturn("fresh-token");

        assertThatThrownBy(() -> appUserService.changePassword(new ChangePasswordRequest()
                .currentPassword("welcome1")
                .newPassword("welcome1")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("exceptionCode", "ERROR:202611060003");
        verify(keycloakUserProvisioner, never()).changePassword("pwrona", "welcome1");
    }

    private void authenticate(String username) {
        Jwt jwt = Jwt.withTokenValue("session-token")
                .header("alg", "RS256")
                .subject(username)
                .issuedAt(Instant.parse("2026-11-06T00:00:00Z"))
                .expiresAt(Instant.parse("2026-11-06T01:00:00Z"))
                .claim("preferred_username", username)
                .build();
        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwt);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, jwt, principal.authorities()));
    }

}
