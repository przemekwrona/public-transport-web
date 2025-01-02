package pl.wrona.webserver.security;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_id_seq")
    @SequenceGenerator(name = "app_user_id_seq", sequenceName = "app_user_id_seq",  allocationSize=1)
    private Long id;

    private String username;
    private String email;
    private String password;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
}
