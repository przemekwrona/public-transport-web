package pl.wrona.webserver.keycloak;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.wrona.webserver.security.AppUser;

@Component
@ConditionalOnProperty(prefix = "keycloak", name = "enabled", havingValue = "false")
public class NoopKeycloakIdentityProvisioner implements KeycloakIdentityProvisioner {

    @Override
    public void provisionAgency(String agencyCode, AppUser owner) {
    }
}
