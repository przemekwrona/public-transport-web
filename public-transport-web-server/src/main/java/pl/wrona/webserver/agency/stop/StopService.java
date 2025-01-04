package pl.wrona.webserver.agency.stop;

import lombok.AllArgsConstructor;
import org.igeolab.iot.agency.api.model.CreateRouteStopTime;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.entity.Stop;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;


    public List<Long> save(List<CreateRouteStopTime> notSavedStops) {
        List<Stop> stops = notSavedStops.stream()
                .map(stop -> Stop.builder()
                        .name(stop.getName())
                        .lon(stop.getLon().doubleValue())
                        .lat(stop.getLat().doubleValue())
                        .build()).toList();

        List<Stop> savedStops = stopRepository.saveAll(stops);

        return savedStops.stream().map(Stop::getOsmId).toList();
    }

    public List<Stop> findStopByIdsIn(List<Long> stopIds) {
        return stopRepository.findAllByOsmIdIn(stopIds);
    }

    public Map<Long, Stop> mapStopByIdsIn(List<Long> stopIds) {
        return findStopByIdsIn(stopIds).stream()
                .collect(Collectors.toMap(Stop::getOsmId, Function.identity()));
    }
}
