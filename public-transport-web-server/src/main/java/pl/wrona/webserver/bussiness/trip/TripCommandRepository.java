package pl.wrona.webserver.bussiness.trip;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;

@Repository
public interface TripCommandRepository extends JpaRepository<TripEntity, Long> {

    void deleteAllByRoute(RouteEntity routeEntity);

}
