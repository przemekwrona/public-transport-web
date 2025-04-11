package pl.wrona.webserver.stops;

import org.igeolab.iot.pt.server.api.model.Stop;

public class BusStopMapper {

    public static Stop apply(BusStop stop) {
        return new Stop()
                .id(stop.getBdot10kId())
                .name(stop.getName())
                .lon(stop.getLon())
                .lat(stop.getLat());
    }
}
