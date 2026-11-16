package pl.wrona.webserver.security;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "login_event")
public class LoginEventEntity {

    @EmbeddedId
    private LoginEventId id;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "is_login", nullable = false)
    private boolean login;

    @Column(name = "is_refresh", nullable = false)
    private boolean refresh;

}
