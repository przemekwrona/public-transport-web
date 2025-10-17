package pl.wrona.webserver.bussiness.trip.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.Trip;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.TripRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripCreatorService {
    private final AgencyService agencyService;
    private final StopService stopService;
    private final TripCreatorRouteService tripCreatorRouteService;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;

    @Transactional
    @PreAgencyAuthorize
    public Status createTrip(String instance, CreateTripDetailsRequest createTripDetailsRequest) {
        Trip tripRequest = createTripDetailsRequest.getTrip().getTrip();
        List<Long> stopIds = tripRequest.getStops().stream()
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);

        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var route = tripCreatorRouteService.findRouteByAgencyAndNameAndLine(agencyEntity, tripRequest.getName(), tripRequest.getLine());

        TripEntity tripEntity = TripMapper.map(tripRequest);
        tripEntity.setRoute(route);
        var lastStop = tripRequest.getStops().stream()
                .reduce((first, second) -> second);


        tripEntity.setDistanceInMeters(lastStop.map(StopTime::getMeters).orElse(0));
        tripEntity.setTravelTimeInSeconds(lastStop.map(StopTime::getSeconds).orElse(0));

        TripEntity savedTrip = tripRepository.save(tripEntity);

        StopTime[] stopTimes = tripRequest.getStops().toArray(StopTime[]::new);

        List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                .mapToObj(i -> {
                    StopTime stopTime = stopTimes[i];

                    StopTimeEntity entity = new StopTimeEntity();

                    StopTimeId stopTimeId = new StopTimeId();
                    stopTimeId.setTripId(savedTrip.getTripId());
                    stopTimeId.setStopSequence(i + 1);
                    entity.setStopTimeId(stopTimeId);

                    entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                    entity.setArrivalSecond(stopTime.getSeconds());
                    entity.setDepartureSecond(stopTime.getSeconds());
                    entity.setDistanceMeters(stopTime.getMeters());

                    return entity;
                }).toList();

        stopTimeRepository.saveAll(entities);

        return new Status()
                .status(Status.StatusEnum.CREATED);
    }

}
