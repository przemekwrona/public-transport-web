package pl.wrona.webserver.agency.mapper;

import lombok.experimental.UtilityClass;
import pl.wrona.webserver.agency.entity.RouteEntity;

import java.util.Optional;

@UtilityClass
public class RouteMapper {

    public org.igeolab.iot.pt.server.api.model.Route map(RouteEntity routeEntity) {
        return Optional.ofNullable(routeEntity)
                .map(routeOptional -> new org.igeolab.iot.pt.server.api.model.Route()
                        .name(routeEntity.getName())
                        .line(routeEntity.getLine())
                        .google(routeEntity.isGoogle())
                        .active(routeEntity.isActive())
                        .origin(routeEntity.getOrigin())
                        .description(routeEntity.getDescription())
                        .destination(routeEntity.getDestination())
                        .via(routeEntity.getVia()))
                .orElse(null);
    }

}
