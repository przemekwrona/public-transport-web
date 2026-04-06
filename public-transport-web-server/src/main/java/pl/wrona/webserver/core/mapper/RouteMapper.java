package pl.wrona.webserver.core.mapper;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.Route;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.igeolab.iot.pt.server.api.model.TerritoryRoute;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;

import java.util.Map;
import java.util.Optional;

@UtilityClass
public class RouteMapper {

    public Route map(RouteEntity routeEntity, Map<Long, StopEntity> stops, Map<Long, RouteEntity> routeWithBrigades, Map<Long, TerritorialUnitEntity> territories) {
        var originStop = Optional.ofNullable(stops.get(routeEntity.getOriginStopId()));
        var destinationStop = Optional.ofNullable(stops.get(routeEntity.getDestinationStopId()));

        var originTerritoryOpt = Optional.ofNullable(territories.get(originStop.map(StopEntity::getTerritorialUnitId).orElse(null)));
        var destinationTerritoryOpt = Optional.ofNullable(territories.get(destinationStop.map(StopEntity::getTerritorialUnitId).orElse(null)));

        return Optional.ofNullable(routeEntity)
                .map(routeOptional -> new Route()
                        .routeId(new RouteId()
                                .line(routeEntity.getLine())
                                .name(routeEntity.getName())
                                .version(routeEntity.getVersion()))
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
                                .lat((float) stops.get(routeEntity.getDestinationStopId()).getLat())
                                .lon((float) stops.get(routeEntity.getDestinationStopId()).getLon()))
                        .originTerritory(new TerritoryRoute()
                                .id(originTerritoryOpt.map(TerritorialUnitEntity::getTerritorialUnitId).orElse(null))
                                .name(originTerritoryOpt.map(TerritorialUnitEntity::getNazwa).orElse(StringUtils.EMPTY))
                                .lat(originTerritoryOpt.map(TerritorialUnitEntity::getCentroidLat).map(Float::parseFloat).orElse(null))
                                .lon(originTerritoryOpt.map(TerritorialUnitEntity::getCentroidLat).map(Float::parseFloat).orElse(null)))
                        .destinationTerritory(new TerritoryRoute()
                                .id(destinationTerritoryOpt.map(TerritorialUnitEntity::getTerritorialUnitId).orElse(null))
                                .name(destinationTerritoryOpt.map(TerritorialUnitEntity::getNazwa).orElse(StringUtils.EMPTY))
                                .lat(destinationTerritoryOpt.map(TerritorialUnitEntity::getCentroidLat).map(Float::parseFloat).orElse(null))
                                .lon(destinationTerritoryOpt.map(TerritorialUnitEntity::getCentroidLon).map(Float::parseFloat).orElse(null)))
                        .description(routeEntity.getDescription())
                        .via(routeEntity.getVia())
                        .matchAnyBrigade(routeWithBrigades.containsKey(routeEntity.getRouteId())))
                .orElse(null);
    }

}
