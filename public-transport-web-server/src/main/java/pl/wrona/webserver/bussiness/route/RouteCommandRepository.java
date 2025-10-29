package pl.wrona.webserver.bussiness.route;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.RouteEntity;

@Repository
public interface RouteCommandRepository extends JpaRepository<RouteEntity, Long> {
}
