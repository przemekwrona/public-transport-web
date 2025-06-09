package pl.wrona.webserver.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRole implements GrantedAuthority {

    @Id
    @Column(name = "app_role_id")
    private String role;

    @Column(name = "description")
    private String description;

    @ManyToMany
    @JoinTable(name = "app_user_app_role",
            joinColumns = @JoinColumn(name = "app_role_id"),
            inverseJoinColumns = @JoinColumn(name = "app_user_id"))
    private Set<AppUser> appUsers;

    @Override
    public String getAuthority() {
        return role;
    }
}
