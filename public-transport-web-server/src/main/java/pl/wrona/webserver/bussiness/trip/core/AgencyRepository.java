package pl.wrona.webserver.bussiness.trip.core;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;
import pl.wrona.webserver.security.AppUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<AgencyEntity, Long> {

    AgencyEntity findByAgencyCodeEquals(String agencyCode);

    Optional<AgencyEntity> findByAppUser(AppUser appUser);

    @Query("SELECT a AS agency, COUNT(r) AS routeCount FROM AgencyEntity a LEFT JOIN a.routeEntities r GROUP BY a")
    List<AgencyRouteCount> countRoutesByAgency();
}
