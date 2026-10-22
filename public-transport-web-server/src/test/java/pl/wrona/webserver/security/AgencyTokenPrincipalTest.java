package pl.wrona.webserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgencyTokenPrincipalTest {

    @Test
    void readsRolesForEachAgency() {
        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwt("pwrona", List.of(
                "/agencies/NEXTSTOP",
                "/agencies/NEXTSTOP/super_user",
                "/agencies/OTHER/DRIVER",
                "/agencies/OTHER/NOT_A_ROLE",
                "/somewhere/else"
        )));

        assertThat(principal.username()).isEqualTo("pwrona");
        assertThat(principal.hasAgency("NEXTSTOP")).isTrue();
        assertThat(principal.hasRole("NEXTSTOP", "SUPER_USER")).isTrue();
        assertThat(principal.hasRole("OTHER", "SUPER_USER")).isFalse();
        assertThat(principal.hasRole("OTHER", "DRIVER")).isTrue();
        assertThat(principal.hasRole("OTHER", "NOT_A_ROLE")).isFalse();
        assertThat(principal.roles()).containsExactly("DRIVER", "SUPER_USER");
    }

    @Test
    void prefersTheAgencyTheUserOwns() {
        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwt("pwrona", List.of(
                "/agencies/BETA/DRIVER",
                "/agencies/ALPHA/AGENCY_OWNER"
        )));

        assertThat(principal.primaryAgencyCode()).contains("ALPHA");
    }

    @Test
    void usesTheSubjectWhenPreferredUsernameIsMissing() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("pwrona")
                .issuedAt(Instant.parse("2026-10-23T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-23T01:00:00Z"))
                .claim("groups", "/agencies/NEXTSTOP/DRIVER")
                .build();

        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwt);

        assertThat(principal.username()).isEqualTo("pwrona");
        assertThat(principal.hasRole("NEXTSTOP", "DRIVER")).isTrue();
    }

    @Test
    void rejectsATokenWithoutAUsername() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.parse("2026-10-23T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-23T01:00:00Z"))
                .claim("groups", List.of("/agencies/NEXTSTOP/DRIVER"))
                .build();

        assertThatThrownBy(() -> AgencyTokenPrincipal.from(jwt))
                .isInstanceOf(BadCredentialsException.class);
    }

    private static Jwt jwt(String username, List<String> groups) {
        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject(username)
                .issuedAt(Instant.parse("2026-10-23T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-23T01:00:00Z"))
                .claim("preferred_username", username)
                .claim("groups", groups)
                .build();
    }

}
