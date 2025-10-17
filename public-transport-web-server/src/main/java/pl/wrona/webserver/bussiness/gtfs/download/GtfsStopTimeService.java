package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class GtfsStopTimeService {

    private final GtfsStopTimeRepository gtfsStopTimeRepository;

    public List<StopTimeEntity> findAllByBrigadeTrip(BrigadeTripEntity brigadeTrip) {
        return gtfsStopTimeRepository.findAllByBrigadeTrip(brigadeTrip);
    }
}
