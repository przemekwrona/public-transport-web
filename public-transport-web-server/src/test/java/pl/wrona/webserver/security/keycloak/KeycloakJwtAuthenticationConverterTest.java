package pl.wrona.webserver.security.keycloak;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import pl.wrona.webserver.security.AgencyTokenPrincipal;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthenticationConverterTest {

    @Test
    void buildsThePrincipalFromTokenGroups() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("pwrona")
                .issuedAt(Instant.parse("2026-10-23T00:00:00Z"))
                .expiresAt(Instant.parse("2026-10-23T01:00:00Z"))
                .claim("preferred_username", "pwrona")
                .claim("groups", List.of("/agencies/NEXTSTOP/SUPER_USER", "/agencies/OTHER/DRIVER"))
                .build();

        AbstractAuthenticationToken authentication = new KeycloakJwtAuthenticationConverter().convert(jwt);

        AgencyTokenPrincipal principal = (AgencyTokenPrincipal) authentication.getPrincipal();
        assertThat(principal.hasRole("NEXTSTOP", "SUPER_USER")).isTrue();
        assertThat(principal.hasRole("OTHER", "SUPER_USER")).isFalse();
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("DRIVER", "SUPER_USER");
    }

}
