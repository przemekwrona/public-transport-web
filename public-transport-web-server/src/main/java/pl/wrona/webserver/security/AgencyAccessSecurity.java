package pl.wrona.webserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

import java.util.stream.Collectors;

@Component
public class AgencyAccessSecurity {

    public boolean checkAccess(String agencyCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUser appUser = (AppUser) authentication.getPrincipal();

        return appUser.getAgencies().stream().map(AgencyEntity::getAgencyCode).collect(Collectors.toSet()).contains(agencyCode);
    }
}
