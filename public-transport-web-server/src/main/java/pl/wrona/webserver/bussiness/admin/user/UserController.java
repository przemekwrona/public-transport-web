package pl.wrona.webserver.bussiness.admin.user;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.UsersApi;
import org.igeolab.iot.pt.server.api.model.AppUsersResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController implements UsersApi {

    private final UserCreatorService userCreatorService;

    @Override
    public ResponseEntity<AppUsersResponse> getAppUsers() {
        return ResponseEntity.ok(userCreatorService.findAllAppUsers());
    }
}
