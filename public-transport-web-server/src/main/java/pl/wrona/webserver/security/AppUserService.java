package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.keycloak.KeycloakPasswordGrant;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class AppUserService {

    private final AppRoleRepository appRoleRepository;
    private final AppUserRepository appUserRepository;
    private final AgencyOwnerRepository agencyOwnerRepository;
    private final KeycloakPasswordGrant keycloakPasswordGrant;

    @Transactional
    public LoginAppUserResponse login(LoginAppUserRequest loginAppUserRequest) {
        String accessToken = keycloakPasswordGrant.accessToken(loginAppUserRequest.getUsername(), loginAppUserRequest.getPassword());
        AppUser user = appUserRepository.findByUsername(loginAppUserRequest.getUsername())
                .orElseThrow(() -> new IllegalStateException("Keycloak user has no application account: " + loginAppUserRequest.getUsername()));

        List<String> roles = appRoleRepository.findAppRolesByAppUsers(Set.of(user)).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var agency = agencyOwnerRepository.findAllByAppUser(user).stream()
                .findFirst()
                .orElse(null);

        return new LoginAppUserResponse()
                .token(accessToken)
                .roles(roles)
                .instance(Optional.ofNullable(agency).map(AgencyEntity::getAgencyCode).orElse("NONE"));
    }

}
