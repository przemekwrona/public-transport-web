package pl.wrona.webserver.agency.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.model.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, String> {

    @Query("SELECT r FROM Route r WHERE r.agency.agencyCode = :agencyCode AND r.routeId = :routeId")
    Route findByAgencyCodeAndRouteId(@Param("agencyCode") String agencyCode, @Param("routeId") String routeId);
}
