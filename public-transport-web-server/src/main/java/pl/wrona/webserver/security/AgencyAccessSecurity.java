package pl.wrona.webserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AgencyAccessSecurity {

    public boolean canRead(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners", "planners", "dispatchers", "viewers"));
    }

    public boolean canReadPlan(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners", "planners", "viewers"));
    }

    public boolean canPlan(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners", "planners"));
    }

    public boolean canReadDispatch(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners", "dispatchers", "viewers"));
    }

    public boolean canDispatch(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners", "dispatchers"));
    }

    public boolean canOwn(String agencyCode) {
        return hasSubgroup(agencyCode, Set.of("owners"));
    }

    private boolean hasSubgroup(String agencyCode, Set<String> subgroups) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || agencyCode == null || agencyCode.isBlank()) {
            return false;
        }
        String prefix = "GROUP_/agencies/" + agencyCode + "/";
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.startsWith(prefix) && subgroups.contains(authority.substring(prefix.length())));
    }
}
