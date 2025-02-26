package pl.wrona.webserver.agency.mapper;

import lombok.experimental.UtilityClass;
import pl.wrona.webserver.agency.entity.Route;

import java.util.Optional;

@UtilityClass
public class RouteMapper {

    public org.igeolab.iot.pt.server.api.model.Route map(Route route) {
        return Optional.ofNullable(route)
                .map(routeOptional -> new org.igeolab.iot.pt.server.api.model.Route()
                        .name(route.getName())
                        .line(route.getLine())
                        .google(route.isGoogle())
                        .active(route.isActive())
                        .origin(route.getOrigin())
                        .description(route.getDescription())
                        .destination(route.getDestination())
                        .via(route.getVia()))
                .orElse(null);
    }

}
