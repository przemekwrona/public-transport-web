package pl.wrona.webserver.security;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoginAuditServiceTest {

    private final LoginEventCommandRepository loginEventCommandRepository = mock(LoginEventCommandRepository.class);
    private final LoginAuditService loginAuditService = new LoginAuditService(loginEventCommandRepository);

    @Test
    void recordStoresTheAttempt() {
        LocalDateTime before = LocalDateTime.now();

        loginAuditService.record("pwrona", "NEXTSTOP", true, true, false);

        ArgumentCaptor<LoginEventEntity> event = ArgumentCaptor.forClass(LoginEventEntity.class);
        verify(loginEventCommandRepository).save(event.capture());
        assertThat(event.getValue().getId().getUsername()).isEqualTo("pwrona");
        assertThat(event.getValue().getId().getAgencyCode()).isEqualTo("NEXTSTOP");
        assertThat(event.getValue().isSuccess()).isTrue();
        assertThat(event.getValue().isLogin()).isTrue();
        assertThat(event.getValue().isRefresh()).isFalse();
        assertThat(event.getValue().getId().getOccurredAt()).isAfterOrEqualTo(before);
    }

    @Test
    void recordUsesNoneWhenAgencyCodeIsBlank() {
        loginAuditService.record("pwrona", "  ", false, false, true);

        ArgumentCaptor<LoginEventEntity> event = ArgumentCaptor.forClass(LoginEventEntity.class);
        verify(loginEventCommandRepository).save(event.capture());
        assertThat(event.getValue().getId().getAgencyCode()).isEqualTo("NONE");
        assertThat(event.getValue().getId().getUsername()).isEqualTo("pwrona");
        assertThat(event.getValue().isSuccess()).isFalse();
        assertThat(event.getValue().isLogin()).isFalse();
        assertThat(event.getValue().isRefresh()).isTrue();
    }

}
