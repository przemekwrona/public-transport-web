package pl.wrona.webserver.agency.mapper;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.Stop;
import pl.wrona.webserver.agency.entity.RouteEntity;
import pl.wrona.webserver.agency.entity.StopEntity;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class RouteMapper {

    public org.igeolab.iot.pt.server.api.model.Route map(RouteEntity routeEntity, Map<Long, StopEntity> stops) {
        return Optional.ofNullable(routeEntity)
                .map(routeOptional -> new org.igeolab.iot.pt.server.api.model.Route()
                        .name(routeEntity.getName())
                        .line(routeEntity.getLine())
                        .google(routeEntity.isGoogle())
                        .active(routeEntity.isActive())
                        .originStop(new Stop()
                                .id(routeEntity.getOriginStopId())
                                .name(routeEntity.getOriginStopName())
                                .lat((float) stops.get(routeEntity.getOriginStopId()).getLat())
                                .lon((float) stops.get(routeEntity.getOriginStopId()).getLon()))
                        .destinationStop(new Stop()
                                .id(routeEntity.getDestinationStopId())
                                .name(routeEntity.getDestinationStopName())
                                .lat((float) stops.get(routeEntity.getOriginStopId()).getLat())
                                .lon((float) stops.get(routeEntity.getOriginStopId()).getLon()))
                        .description(routeEntity.getDescription())
                        .via(routeEntity.getVia()))
                .orElse(null);
    }

}
