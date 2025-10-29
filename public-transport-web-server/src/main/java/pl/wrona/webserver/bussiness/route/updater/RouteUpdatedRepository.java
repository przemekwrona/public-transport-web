package pl.wrona.webserver.bussiness.route.updater;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.RouteEntity;

@Repository
public interface RouteUpdatedRepository extends JpaRepository<RouteEntity, Long> {
}
