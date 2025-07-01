package pl.wrona.webserver.bussiness.admin.user.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.security.AppRole;

import java.util.Set;

@Repository
public interface UserCreatorAppRolesRepository extends JpaRepository<AppRole, String> {

    Set<AppRole> findAllByRoleIsIn(Set<String> roles);

}
