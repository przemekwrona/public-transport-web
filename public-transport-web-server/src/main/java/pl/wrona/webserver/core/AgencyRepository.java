package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.entity.Agency;
import pl.wrona.webserver.security.AppUser;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    List<Agency> findAllByAgencyCodeEndingWith(String suffix);

    Agency findByAgencyCodeEquals(String agencyCode);

    Agency findByAppUser(AppUser appUser);

    @Query("SELECT a AS agency, COUNT(r) AS routeCount FROM Agency a LEFT JOIN a.routeEntities r GROUP BY a")
    List<AgencyRouteCount> countRoutesByAgency();
}
