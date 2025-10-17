package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

import java.util.List;

@Repository
public interface GtfsRouteRepository extends JpaRepository<RouteEntity, Long> {

    List<RouteEntity> findAllByAgency(AgencyEntity agency);
}
