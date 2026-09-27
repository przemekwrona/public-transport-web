package pl.wrona.webserver.keycloak;

import pl.wrona.webserver.security.AppUser;

public interface KeycloakIdentityProvisioner {

    void provisionAgency(String agencyCode, AppUser owner);
}
