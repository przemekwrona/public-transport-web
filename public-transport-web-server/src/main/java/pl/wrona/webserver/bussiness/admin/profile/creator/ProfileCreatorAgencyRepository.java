package pl.wrona.webserver.bussiness.admin.profile.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;

@Repository
public interface ProfileCreatorAgencyRepository extends JpaRepository<AgencyEntity, Long> {
}
