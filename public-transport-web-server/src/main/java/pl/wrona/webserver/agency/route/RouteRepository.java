package pl.wrona.webserver.agency.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.model.Route;
import pl.wrona.webserver.agency.model.RouteId;

@Repository
public interface RouteRepository extends JpaRepository<Route, RouteId> {
}
