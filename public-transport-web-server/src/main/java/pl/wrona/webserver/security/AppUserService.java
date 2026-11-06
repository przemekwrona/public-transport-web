package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.ChangePasswordRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.keycloak.KeycloakLoginClient;
import pl.wrona.webserver.security.keycloak.KeycloakUserProvisioner;

@Service
@AllArgsConstructor
public class AppUserService {

    private final KeycloakLoginClient keycloakLoginClient;
    private final JwtDecoder jwtDecoder;
    private final KeycloakUserProvisioner keycloakUserProvisioner;

    public LoginAppUserResponse login(LoginAppUserRequest loginAppUserRequest) {
        String accessToken = keycloakLoginClient.login(loginAppUserRequest.getUsername(), loginAppUserRequest.getPassword());
        AgencyTokenPrincipal principal = AgencyTokenPrincipal.from(jwtDecoder.decode(accessToken));

        return new LoginAppUserResponse()
                .token(accessToken)
                .roles(principal.roles())
                .instance(principal.primaryAgencyCode().orElse("NONE"));
    }

    public Status changePassword(ChangePasswordRequest request) {
        String currentPassword = request.getCurrentPassword();
        String newPassword = request.getNewPassword();
        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new BusinessException("ERROR:202611060002", "Password is required");
        }
        String username = currentUsername();
        try {
            keycloakLoginClient.login(username, currentPassword);
        } catch (BadCredentialsException exception) {
            throw new BusinessException("ERROR:202611060001", "Current password is invalid");
        }
        if (currentPassword.equals(newPassword)) {
            throw new BusinessException("ERROR:202611060003", "New password must differ from the current password");
        }
        keycloakUserProvisioner.changePassword(username, newPassword);
        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AgencyTokenPrincipal principal) {
            return principal.username();
        }
        throw new BusinessException("ERROR:202611060005", "Signed-in user is missing");
    }

}
