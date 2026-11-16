package pl.wrona.webserver.bussiness.admin.login.pagination;

import org.igeolab.iot.pt.server.api.model.LoginEventsResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pl.wrona.webserver.security.LoginEventCommandRepository;
import pl.wrona.webserver.security.LoginEventEntity;
import pl.wrona.webserver.security.LoginEventId;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginEventPaginationServiceTest {

    private final LoginEventCommandRepository loginEventCommandRepository = mock(LoginEventCommandRepository.class);
    private final LoginEventPaginationService service = new LoginEventPaginationService(loginEventCommandRepository);

    @Test
    void findLatestReturnsTheNewestPage() {
        LocalDateTime occurredAt = LocalDateTime.parse("2026-11-16T20:00:00");
        LoginEventEntity event = LoginEventEntity.builder()
                .id(LoginEventId.builder()
                        .occurredAt(occurredAt)
                        .agencyCode("NEXTSTOP")
                        .username("pwrona")
                        .build())
                .success(true)
                .login(true)
                .refresh(false)
                .build();
        when(loginEventCommandRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), PageRequest.of(0, 20), 21));

        LoginEventsResponse response = service.findLatest(0, 20);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(loginEventCommandRepository).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(20);
        assertThat(pageable.getValue().getSort().getOrderFor("id.occurredAt").getDirection()).isEqualTo(Sort.Direction.DESC);
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(21);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getEvents()).hasSize(1);
        assertThat(response.getEvents().get(0).getUsername()).isEqualTo("pwrona");
        assertThat(response.getEvents().get(0).getAgencyCode()).isEqualTo("NEXTSTOP");
        assertThat(response.getEvents().get(0).getOccurredAt()).isEqualTo(occurredAt);
        assertThat(response.getEvents().get(0).getSuccess()).isTrue();
        assertThat(response.getEvents().get(0).getLogin()).isTrue();
        assertThat(response.getEvents().get(0).getRefresh()).isFalse();
    }

    @Test
    void findLatestClampsThePageSize() {
        when(loginEventCommandRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        service.findLatest(-1, 500);

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(loginEventCommandRepository).findAll(pageable.capture());
        assertThat(pageable.getValue().getPageNumber()).isZero();
        assertThat(pageable.getValue().getPageSize()).isEqualTo(100);
    }

}
