package pl.wrona.webserver.bussiness.route.creator;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.RouteEntity;

@Repository
interface RouteCreatorRepository extends JpaRepository<RouteEntity, Long> {
}
