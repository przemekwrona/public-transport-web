package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.security.AppRole;
import pl.wrona.webserver.security.AppUser;

import java.util.Set;

@Repository
public interface ProfileCreatorAppUserRepository extends JpaRepository<AppUser, Long> {

    Set<AppUser> findByAppRolesIsIn(Set<AppRole> appRoles);
}
