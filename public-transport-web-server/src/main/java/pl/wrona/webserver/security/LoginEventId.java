package pl.wrona.webserver.security;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class LoginEventId implements Serializable {

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "agency_code", nullable = false, length = 15)
    private String agencyCode;

    @Column(name = "username", nullable = false, length = 60)
    private String username;

}
