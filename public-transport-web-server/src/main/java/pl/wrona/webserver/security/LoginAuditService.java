package pl.wrona.webserver.security;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LoginAuditService {

    private final LoginEventCommandRepository loginEventCommandRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String username, String agencyCode, boolean success, boolean login, boolean refresh) {
        loginEventCommandRepository.save(LoginEventEntity.builder()
                .id(LoginEventId.builder()
                        .occurredAt(LocalDateTime.now())
                        .agencyCode(agencyCodeOrNone(agencyCode))
                        .username(username == null ? "" : username)
                        .build())
                .success(success)
                .login(login)
                .refresh(refresh)
                .build());
    }

    private static String agencyCodeOrNone(String agencyCode) {
        if (agencyCode == null || agencyCode.isBlank()) {
            return "NONE";
        }
        return agencyCode;
    }

}
