package pl.wrona.webserver.bussiness.admin.user.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppUser;
import pl.wrona.webserver.security.AppUserRepository;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@AllArgsConstructor
public class UserCreatorService {

    private final PasswordEncoder encoder;
    private final AppUserRepository appUserRepository;
    private final UserCreatorAppRolesRepository userCreatorAppRolesRepository;


    @Transactional
    public CreateAppUserResponse createUser(CreateAppUserRequest createAppUserRequest) {
        Set<AppRole> defaultAgencyRole = userCreatorAppRolesRepository.findAllByRoleIsIn(Set.of("AGENCY_OWNER"));

        AppUser appUser = new AppUser();
        appUser.setUsername(createAppUserRequest.getUsername());
        appUser.setEmail(createAppUserRequest.getEmail());
        appUser.setPassword(encoder.encode(createAppUserRequest.getPassword()));
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setUpdatedAt(LocalDateTime.now());
        appUser.setAppRoles(defaultAgencyRole);

        appUser.setAccountNonExpired(true);
        appUser.setAccountNonLocked(true);
        appUser.setCredentialsNonExpired(true);
        appUser.setEnabled(true);

        appUserRepository.save(appUser);

        return new CreateAppUserResponse()
                .username(createAppUserRequest.getUsername());
    }
}
