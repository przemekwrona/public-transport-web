package pl.wrona.webserver.bussiness.admin.user.pagination;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.security.AppUser;

@Repository
public interface UserPaginationRepository extends JpaRepository<AppUser, Long> {
}
