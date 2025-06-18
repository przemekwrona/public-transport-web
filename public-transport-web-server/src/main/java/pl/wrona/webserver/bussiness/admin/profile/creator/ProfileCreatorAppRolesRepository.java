package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppUser;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface ProfileCreatorAppRolesRepository extends JpaRepository<AppRole, String> {

    Set<AppRole> findAllByRoleIsIn(Set<String> roles);

}
