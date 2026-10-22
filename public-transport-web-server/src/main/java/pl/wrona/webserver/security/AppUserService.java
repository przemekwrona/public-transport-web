package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.security.keycloak.KeycloakLoginClient;

@Service
@AllArgsConstructor
public class AppUserService {

    private final KeycloakLoginClient keycloakLoginClient;
    private final JwtDecoder jwtDecoder;

    public LoginAppUserResponse login(LoginAppUserRequest loginAppUserRequest) {
        String accessToken = keycloakLoginClient.login(loginAppUserRequest.getUsername(), loginAppUserRequest.getPassword());
        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwtDecoder.decode(accessToken));

        return new LoginAppUserResponse()
                .token(accessToken)
                .roles(principal.roles())
                .instance(principal.primaryAgencyCode().orElse("NONE"));
    }

}
