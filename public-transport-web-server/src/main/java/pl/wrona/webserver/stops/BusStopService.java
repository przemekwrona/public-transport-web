package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CenterPoint;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopsPatchRequest;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;

import java.util.Comparator;
import java.util.Objects;

@Service
@AllArgsConstructor
public class BusStopService {

    private final BusStopRepository busStopRepository;
    private final AgencyService agencyService;

    public StopsResponse findBusStop(float maxLat, float minLon, float minLat, float maxLon) {
        var stops = busStopRepository.findAllByArea(maxLat, minLon, minLat, maxLon).stream()
                .filter(Objects::nonNull)
                .map(BusStopMapper::apply)
                .toList();

        return new StopsResponse()
                .stops(stops);
    }

    public StopsResponse findStopsByStopName(String stopName) {
        var stops = busStopRepository.findBusStopByNameStartsWith(stopName).stream()
                .map(BusStopMapper::apply)
                .sorted(Comparator.comparingInt(stop -> stop.getName().length()))
                .toList();

        return new StopsResponse()
                .stops(stops);
    }

    public Status patchStop(StopsPatchRequest stopsPatchRequest) {
        busStopRepository.findById(stopsPatchRequest.getId()).ifPresent(busStop -> {
            busStop.setName(stopsPatchRequest.getName());
            busStop.setActive(stopsPatchRequest.getActive());

            busStopRepository.save(busStop);
        });
        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    public CenterPoint centerMap() {
        AgencyEntity agencyEntity = agencyService.getLoggedAgency();
        return new CenterPoint()
                .latitude(agencyEntity.getLatitude())
                .longitude(agencyEntity.getLongitude())
                .zoom(14);
    }
}
