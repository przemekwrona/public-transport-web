package pl.wrona.webserver.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;

import java.util.List;

@Repository
public interface AgencyRepository extends JpaRepository<AgencyEntity, Long> {

    AgencyEntity findByAgencyCodeEquals(String agencyCode);

    AgencyEntity findByAppUser(AppUser appUser);

    @Query("SELECT a AS agency, COUNT(r) AS routeCount FROM AgencyEntity a LEFT JOIN a.routeEntities r GROUP BY a")
    List<AgencyRouteCount> countRoutesByAgency();
}
