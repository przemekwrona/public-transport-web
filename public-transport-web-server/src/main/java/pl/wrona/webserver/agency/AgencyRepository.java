package pl.wrona.webserver.agency;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;
import pl.wrona.webserver.security.AppUser;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    List<Agency> findAllByAgencyCodeEndingWith(String suffix);

    Agency findByAgencyCodeEquals(String agencyCode);

    Agency findByAppUser(AppUser appUser);
}
