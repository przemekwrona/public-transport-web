package pl.wrona.webserver.bussiness.admin.user.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AppUser;
import org.igeolab.iot.pt.server.api.model.AppUsersResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserPaginationService {

    private final UserPaginationRepository userPaginationRepository;

    public AppUsersResponse findAllAppUsers(String search) {
        AppUsersResponse appUsersResponse = new AppUsersResponse();

        userPaginationRepository.findAllByUsernameContainsOrEmailContains(search, search).stream()
                .map(user -> new AppUser()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .createdAt(user.getCreatedAt()))
                .forEach(appUsersResponse::addUsersItem);

        return appUsersResponse;
    }
}
