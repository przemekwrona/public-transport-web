package pl.wrona.webserver.bussiness.admin.user;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.UsersApi;
import org.igeolab.iot.pt.server.api.model.AppUsersResponse;
import org.igeolab.iot.pt.server.api.model.CreateAppUserRequest;
import org.igeolab.iot.pt.server.api.model.CreateAppUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.admin.user.creator.UserCreatorService;
import pl.wrona.webserver.bussiness.admin.user.pagination.UserPaginationService;
import pl.wrona.webserver.security.AppUserService;

@RestController
@AllArgsConstructor
public class UserController implements UsersApi {

    private final UserPaginationService userPaginationService;
    private final UserCreatorService userCreatorService;

    @Override
    public ResponseEntity<CreateAppUserResponse> createUser(CreateAppUserRequest createAppUserRequest) {
        return ResponseEntity.ok(userCreatorService.createUser(createAppUserRequest));
    }

    @Override
    public ResponseEntity<AppUsersResponse> getAppUsers() {
        return ResponseEntity.ok(userPaginationService.findAllAppUsers());
    }
}
