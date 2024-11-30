package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BusStopService {

    private final BusStopRepository busStopRepository;

    public StopsResponse findBusStop(float maxLat, float minLon, float minLat, float maxLon) {
        var stops = busStopRepository.findAllByArea(maxLat, minLon, minLat, maxLon).stream()
                .map(stop -> new Stop()
                        .id(stop.getOsmId())
                        .name(stop.getName())
                        .ref(stop.getRef())
                        .lon(stop.getLon())
                        .lat(stop.getLat()))
                .toList();

        return new StopsResponse()
                .stops(stops);
    }
}
