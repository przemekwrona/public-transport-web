package pl.wrona.webserver.stops;

import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.Stop;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;

import java.util.Optional;

public class BusStopMapper {

    public static Stop apply(BusStop stop) {
        return new Stop()
                .id(stop.getStopId())
                .bdot10kId(stop.getBdot10kId())
                .osmId(stop.getOsmId())
                .name(stop.getName())
                .lon((float) stop.getLon())
                .lat((float) stop.getLat())
                .isBdot10k(stop.isBdot10k())
                .isOsm(stop.isOsm())
                .territoryOsmId(Optional.of(stop)
                        .map(BusStop::getTerritorialUnit)
                        .map(TerritorialUnitEntity::getLokalnyid)
                        .orElse(StringUtils.EMPTY));
    }
}
