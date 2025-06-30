package pl.wrona.webserver.security;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;


@Entity
@Builder
@Getter
@Setter
@Table(name = "app_user")
@NoArgsConstructor
@AllArgsConstructor
public class AppUser implements UserDetails {

    @Id
    @Column(name = "app_user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "app_user_id_seq")
    @SequenceGenerator(name = "app_user_id_seq", sequenceName = "app_user_id_seq", allocationSize = 1)
    private Long id;

    private String username;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @ManyToMany(mappedBy = "appUsers", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Set<AppRole> appRoles;

    @ManyToMany(mappedBy = "users")
    private Set<AgencyEntity> agencies;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.appRoles.stream().toList();
    }
}
