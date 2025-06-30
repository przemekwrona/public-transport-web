package pl.wrona.webserver.bussiness.admin.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.security.AppUser;

@Repository
public interface UserCreatorRepository extends JpaRepository<AppUser, Long> {
}
