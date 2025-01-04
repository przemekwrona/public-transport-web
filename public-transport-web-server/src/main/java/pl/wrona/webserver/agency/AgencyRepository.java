package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.security.AppUser;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    Agency findByAgencyCodeEquals(String agencyCode);

    Agency findByAppUser(AppUser appUser);
}
