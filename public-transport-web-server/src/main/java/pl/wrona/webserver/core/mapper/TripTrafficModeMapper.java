package pl.wrona.webserver.core.mapper;

import lombok.experimental.UtilityClass;
import org.igeolab.iot.pt.server.api.model.TrafficMode;
import pl.wrona.webserver.core.agency.TripTrafficMode;

@UtilityClass
public class TripTrafficModeMapper {

    public TripTrafficMode map(TrafficMode mode) {
        if (TrafficMode.NORMAL.equals(mode)) {
            return TripTrafficMode.NORMAL;
        } else if (TrafficMode.TRAFFIC.equals(mode)) {
            return TripTrafficMode.TRAFFIC;
        } else {
            return null;
        }
    }

    public TrafficMode map(TripTrafficMode mode) {
        if (TripTrafficMode.NORMAL.equals(mode)) {
            return TrafficMode.NORMAL;
        } else if (TripTrafficMode.TRAFFIC.equals(mode)) {
            return TrafficMode.TRAFFIC;
        } else {
            return null;
        }
    }
}
