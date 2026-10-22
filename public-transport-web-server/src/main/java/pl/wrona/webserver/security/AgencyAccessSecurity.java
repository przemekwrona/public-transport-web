package pl.wrona.webserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AgencyAccessSecurity {

    public boolean checkAccess(String agencyCode) {
        return principal().map(user -> user.hasAgency(agencyCode)).orElse(false);
    }

    public boolean hasRole(String agencyCode, String role) {
        return principal().map(user -> user.hasRole(agencyCode, role)).orElse(false);
    }

    private Optional<AgencyTokenPrincipal> principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AgencyTokenPrincipal user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

}
