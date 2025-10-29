package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class GtfsRouteService {

    private GtfsRouteRepository gtfsRouteRepository;


    public List<RouteEntity> findAllRoutes(AgencyEntity agency) {
        return gtfsRouteRepository.findAllByAgency(agency);
    }
}
