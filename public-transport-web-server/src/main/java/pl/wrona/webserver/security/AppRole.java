package pl.wrona.webserver.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppRole {

    @Id
    @Column(name = "app_role_id")
    private String role;

    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "appRoles")
    private Set<AppUser> appUsers;
}
