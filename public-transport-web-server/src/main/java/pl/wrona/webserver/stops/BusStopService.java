package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BusStopService {

    private final BusStopRepository busStopRepository;

    public StopsResponse findBusStop(float maxLat, float minLon, float minLat, float maxLon) {
        var stops = busStopRepository.findAllByArea(maxLat, minLon, minLat, maxLon).stream()
                .map(BusStopMapper::apply)
                .toList();

        return new StopsResponse()
                .stops(stops);
    }

    public StopsResponse findStopsByStopName(String stopName) {
        var stops = busStopRepository.findBusStopByNameStartsWith(stopName).stream()
                .map(BusStopMapper::apply)
                .toList();

        return new StopsResponse()
                .stops(stops);
    }
}
