package pl.wrona.webserver.bussiness.trip.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CreateTripDetailsRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripProfileCommandService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.bussiness.trip.TripSequenceQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.bussiness.trip.TripRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripCreatorService {
    private final StopService stopService;
    private final TripRepository tripRepository;
    private final StopTimeRepository stopTimeRepository;
    private final TripQueryService tripQueryService;
    private final RouteQueryService routeQueryService;
    private final TripSequenceQueryService tripSequenceQueryService;
    private final TripProfileCommandService tripProfileCommandService;

    @Transactional
    @PreAgencyAuthorize
    public Status createTrip(String instance, String routeCode, CreateTripDetailsRequest createTripDetailsRequest) {
        boolean uniqueTripIndexExists = tripQueryService.existsUniqueTripIndex(instance,
                createTripDetailsRequest.getBody().getTripId().getRouteId().getLine(),
                createTripDetailsRequest.getBody().getTripId().getRouteId().getName(),
                createTripDetailsRequest.getBody().getTripId().getVariantName(),
                TripVariantModeMapper.map(createTripDetailsRequest.getBody().getTripId().getVariantMode()));

        if (uniqueTripIndexExists) {
            throw new BusinessException("ERROR:202510300047", "Trip index already exists");
        }

        TripsDetails tripRequest = createTripDetailsRequest.getBody();
        List<Long> stopIds = tripRequest.getTripProfiles().stream()
                .flatMap(profile -> profile.getStops().stream())
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);

        var route = routeQueryService.findRouteByAgencyCodeAndRouteCode(instance, routeCode);

        TripEntity tripEntity = TripMapper.map(tripRequest);
        tripEntity.setRoute(route);

        var nextSequence = tripSequenceQueryService.findNextValue(instance, route.getRouteSequence());
        tripEntity.setTripSequence(nextSequence);
        tripEntity.setTripCode(Hex.toHex3(nextSequence));

        LocalDateTime now = LocalDateTime.now();
        tripEntity.setCreatedAt(now);
        tripEntity.setUpdatedAt(now);

        TripEntity savedTrip = tripRepository.save(tripEntity);

        for (TripProfile tripProfile : tripRequest.getTripProfiles()) {
            TripProfileEntity tripProfileEntity = new TripProfileEntity();
            tripProfileEntity.setTrip(savedTrip);
            tripProfileEntity.setTrafficMode(TripTrafficModeMapper.map(tripProfile.getTrafficMode()));
            tripProfileEntity.setTravelTimeInSeconds(0);
            tripProfileEntity.setCalculatedCommunicationVelocity(tripProfile.getCustomizedCommunicationVelocity());
            tripProfileEntity.setCustomizedCommunicationVelocity(tripProfile.getCustomizedCommunicationVelocity());
            tripProfileEntity.setDefaultProfile(tripProfile.getIsDefault());
            tripProfileEntity.setCustomized(tripProfile.getIsCustomized());

            var savedTripProfile = tripProfileCommandService.save(tripProfileEntity);

            StopTime[] stopTimes = tripProfile.getStops().toArray(StopTime[]::new);

            List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                    .mapToObj(i -> {
                        StopTime stopTime = stopTimes[i];

                        StopTimeEntity entity = new StopTimeEntity();

                        StopTimeId stopTimeId = new StopTimeId();
                        stopTimeId.setTripProfileId(savedTripProfile.getTripProfileId());
                        stopTimeId.setStopSequence(i + 1);
                        entity.setStopTimeId(stopTimeId);
                        entity.setTripProfile(savedTripProfile);

                        entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                        entity.setCalculatedTimeSeconds(stopTime.getCalculatedSeconds());
                        entity.setCustomizedTimeSeconds(Optional.ofNullable(stopTime.getCustomizedSeconds()).orElse(stopTime.getCalculatedSeconds()));
                        entity.setDistanceMeters(stopTime.getMeters());

                        return entity;
                    }).toList();

            stopTimeRepository.saveAll(entities);
        }

        return new Status()
                .status(Status.StatusEnum.CREATED);
    }

}
