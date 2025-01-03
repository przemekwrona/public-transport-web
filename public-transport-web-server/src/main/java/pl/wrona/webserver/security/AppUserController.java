package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AuthApi;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AppUserController implements AuthApi {

    private final AppUserService appUserService;

    @Override
    public ResponseEntity<CreateAppUserResponse> createUser(CreateAppUserRequest createAppUserRequest) {
        return ResponseEntity.accepted().body(appUserService.createUser(createAppUserRequest));
    }

    @Override
    public ResponseEntity<LoginAppUserResponse> login(LoginAppUserRequest loginAppUserRequest) {
        return ResponseEntity.ok(appUserService.login(loginAppUserRequest));
    }
}
