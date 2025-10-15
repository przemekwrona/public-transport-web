package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.config.AuthTokenUtils;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@AllArgsConstructor
public class AppUserService {

    private final AppRoleRepository appRoleRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthTokenUtils authTokenUtils;
    private final AgencyOwnerRepository agencyOwnerRepository;

    @Transactional
    public LoginAppUserResponse login(LoginAppUserRequest loginAppUserRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginAppUserRequest.getUsername(), loginAppUserRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        AppUser userDetails = (AppUser) authentication.getPrincipal();

        List<String> roles = appRoleRepository.findAppRolesByAppUsers(Set.of(userDetails)).stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        var agency = agencyOwnerRepository.findAllByAppUser(userDetails);

        return new LoginAppUserResponse()
                .token(authTokenUtils.generateJwtToken(userDetails))
                .roles(roles)
                .instance(Optional.ofNullable(agency).map(AgencyEntity::getAgencyCode).orElse("NONE"));
    }

}
