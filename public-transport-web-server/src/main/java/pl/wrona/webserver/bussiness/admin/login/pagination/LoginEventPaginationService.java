package pl.wrona.webserver.bussiness.admin.login.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.LoginEventItem;
import org.igeolab.iot.pt.server.api.model.LoginEventsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.security.LoginEventEntity;
import pl.wrona.webserver.security.LoginEventQueryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class LoginEventPaginationService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final LoginEventQueryRepository loginEventQueryRepository;

    @PreAuthorize("hasAuthority('SUPER_USER')")
    @Transactional(readOnly = true)
    public LoginEventsResponse findLatest(Integer page, Integer size) {
        int safePage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Page<LoginEventEntity> result = loginEventQueryRepository.findAll(PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "id.occurredAt")));

        List<LoginEventItem> events = result.getContent().stream()
                .map(LoginEventPaginationService::toItem)
                .toList();

        return new LoginEventsResponse()
                .events(events)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages());
    }

    private static LoginEventItem toItem(LoginEventEntity event) {
        return new LoginEventItem()
                .occurredAt(event.getId().getOccurredAt())
                .agencyCode(event.getId().getAgencyCode())
                .username(event.getId().getUsername())
                .success(event.isSuccess())
                .login(event.isLogin())
                .refresh(event.isRefresh());
    }

}
