package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    @Transactional
    public CreateAppUserResponse createUser(CreateAppUserRequest createAppUserRequest) {
        AppUser appUser = new AppUser();
        appUser.setUsername(createAppUserRequest.getUsername());
        appUser.setEmail(createAppUserRequest.getEmail());
        appUser.setPassword(createAppUserRequest.getPassword());
        appUser.setCreatedAt(LocalDateTime.now());
        appUser.setUpdatedAt(LocalDateTime.now());

        appUser.setAccountNonExpired(true);
        appUser.setAccountNonLocked(true);
        appUser.setCredentialsNonExpired(true);
        appUser.setEnabled(true);

        appUserRepository.save(appUser);

        return new CreateAppUserResponse()
                .username(createAppUserRequest.getUsername());
    }
}
