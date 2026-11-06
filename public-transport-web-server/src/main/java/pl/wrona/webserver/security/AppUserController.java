package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.AuthApi;
import org.igeolab.iot.pt.server.api.model.ChangePasswordRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserRequest;
import org.igeolab.iot.pt.server.api.model.LoginAppUserResponse;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class AppUserController implements AuthApi {

    private final AppUserService appUserService;

    @Override
    public ResponseEntity<LoginAppUserResponse> login(LoginAppUserRequest loginAppUserRequest) {
        return ResponseEntity.ok(appUserService.login(loginAppUserRequest));
    }

    @Override
    public ResponseEntity<Status> changePassword(ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(appUserService.changePassword(changePasswordRequest));
    }
}
