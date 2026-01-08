package pl.wrona.webserver.bussiness.route.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.ModificationRouteResponse;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.route.LineNameCleaner;
import pl.wrona.webserver.bussiness.route.RouteCommandService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class RouteCreatorService {

    private final StopService stopService;
    private final AgencyService agencyService;
    private final RouteCommandService routeCommandService;
    private final RouteQueryService routeQueryService;

    @Transactional
    @PreAgencyAuthorize
    public ModificationRouteResponse createRoute(String instance, Route route) {
        var stopDictionary = stopService.mapStopByIdsIn(List.of(route.getOriginStop().getId(), route.getDestinationStop().getId()));

        var lastInsertedVersion = routeQueryService.findLastInsertedVersion(instance, route.getRouteId().getLine(), route.getRouteId().getName());

        RouteEntity unsavedRouteEntity = new RouteEntity();

        unsavedRouteEntity.setLine(route.getRouteId().getLine());
        unsavedRouteEntity.setName(LineNameCleaner.clean(route.getRouteId().getName()));
        unsavedRouteEntity.setOriginName(route.getRouteId().getName());
        unsavedRouteEntity.setVersion(++lastInsertedVersion);

        unsavedRouteEntity.setOriginStopId(route.getOriginStop().getId());
        unsavedRouteEntity.setOriginStopName(stopDictionary.get(route.getOriginStop().getId()).getName());

        unsavedRouteEntity.setDestinationStopId(route.getDestinationStop().getId());
        unsavedRouteEntity.setDestinationStopName(stopDictionary.get(route.getDestinationStop().getId()).getName());

        unsavedRouteEntity.setVia(route.getVia());
        unsavedRouteEntity.setGoogle(route.getGoogle());
        unsavedRouteEntity.setActive(route.getActive());

        LocalDateTime now = LocalDateTime.now();

        unsavedRouteEntity.setCreatedAt(now);
        unsavedRouteEntity.setUpdatedAt(now);

        unsavedRouteEntity.setAgency(agencyService.findAgencyByAgencyCode(instance));

        RouteEntity savedRoute = routeCommandService.save(unsavedRouteEntity);

        return new ModificationRouteResponse()
                .status(new Status()
                        .status(Status.StatusEnum.CREATED))
                .routeId(new RouteId()
                        .line(savedRoute.getLine())
                        .name(savedRoute.getName())
                        .version(savedRoute.getVersion()));
    }


}
