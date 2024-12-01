package pl.wrona.webserver.agency.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.model.Agency;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    Agency findByAgencyCodeEquals(String agencyCode);
}
