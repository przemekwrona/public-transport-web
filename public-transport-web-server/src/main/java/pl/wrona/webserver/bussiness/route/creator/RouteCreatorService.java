package pl.wrona.webserver.bussiness.route.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.LineNameCleaner;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class RouteCreatorService {

    private final StopService stopService;
    private final AgencyService agencyService;
    private final RouteCreatorRepository routeCreatorRepository;

    @Transactional
    @PreAgencyAuthorize
    public Status createRoute(String instance, Route route) {
        var stopDictionary = stopService.mapStopByIdsIn(List.of(route.getOriginStop().getId(), route.getDestinationStop().getId()));

        RouteEntity unsavedRouteEntity = new RouteEntity();

        unsavedRouteEntity.setLine(route.getLine());
        unsavedRouteEntity.setName(LineNameCleaner.clean(route.getName()));
        unsavedRouteEntity.setOriginName(route.getName());

        unsavedRouteEntity.setOriginStopId(route.getOriginStop().getId());
        unsavedRouteEntity.setOriginStopName(stopDictionary.get(route.getOriginStop().getId()).getName());

        unsavedRouteEntity.setDestinationStopId(route.getDestinationStop().getId());
        unsavedRouteEntity.setDestinationStopName(stopDictionary.get(route.getDestinationStop().getId()).getName());

        unsavedRouteEntity.setVia(route.getVia());
        unsavedRouteEntity.setGoogle(route.getGoogle());
        unsavedRouteEntity.setActive(route.getActive());


        unsavedRouteEntity.setAgency(agencyService.findAgencyByAgencyCode(instance));

        routeCreatorRepository.save(unsavedRouteEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }


}
