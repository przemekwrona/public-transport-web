package pl.wrona.webserver.bussiness.admin.login;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.LoginEventsApi;
import org.igeolab.iot.pt.server.api.model.LoginEventsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.admin.login.pagination.LoginEventPaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class LoginEventController implements LoginEventsApi {

    private final LoginEventPaginationService loginEventPaginationService;

    @Override
    public ResponseEntity<LoginEventsResponse> getLoginEvents(Integer page, Integer size) {
        return ResponseEntity.ok(loginEventPaginationService.findLatest(page, size));
    }

}
