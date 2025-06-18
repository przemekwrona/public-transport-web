package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;

@Repository
public interface ProfileCreatorAgencyRepository extends JpaRepository<Agency, Long> {
}
