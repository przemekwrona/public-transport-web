package pl.wrona.webserver.bussiness.trip.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;
import pl.wrona.webserver.bussiness.trip.core.agency.RouteEntity;

@Repository
public interface TripCreatorRouteRepository extends JpaRepository<RouteEntity, Long> {

    RouteEntity findByAgencyAndNameAndLine(AgencyEntity agency, String name, String line);
}
