package pl.wrona.webserver.bussiness.trip.updater;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.UpdateTripDetailsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.stoptime.StopTimeCommandService;
import pl.wrona.webserver.bussiness.trip.TripCommandService;
import pl.wrona.webserver.bussiness.trip.TripProfileCommandService;
import pl.wrona.webserver.bussiness.trip.TripProfileQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.StopTimeId;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripProfileMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripUpdaterService {

    private final TripQueryService tripQueryService;
    private final StopTimeCommandService stopTimeCommandService;
    private final StopService stopService;
    private final TripCommandService tripCommandService;
    private final TripProfileQueryService tripProfileQueryService;
    private final TripProfileCommandService tripProfileCommandService;

    @Transactional
    @PreAgencyAuthorize
    public Status updateTrip(String instance, String routeCode, String tripCode, UpdateTripDetailsRequest updateTripDetailsRequest) {
        var tripDetails = updateTripDetailsRequest.getBody();

        List<Long> stopIds = tripDetails.getTripProfiles().stream()
                .flatMap(tripProfile -> tripProfile.getStops().stream())
                .map(StopTime::getStopId)
                .toList();

        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds);
        TripEntity tripEntity = tripQueryService.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);

        TripEntity updatedTrip = TripMapper.update(tripEntity, tripDetails);

        for (TripProfile tripProfile : tripDetails.getTripProfiles()) {
            TripProfileEntity existedTripProfileEntity = tripProfileQueryService.findAllByTripAndTrafficMode(tripEntity, TripTrafficModeMapper.map(tripProfile.getTrafficMode()));

            if (existedTripProfileEntity == null) {
                TripProfileEntity updatedTripProfile = TripProfileMapper.map(tripProfile);
                updatedTripProfile.setTrip(tripEntity);
                tripProfileCommandService.save(updatedTripProfile);
            } else {
                TripProfileEntity updatedTripProfile = TripProfileMapper.update(existedTripProfileEntity, tripProfile);
                tripProfileCommandService.save(updatedTripProfile);
            }

            TripProfileEntity tripProfileEntity = tripProfileQueryService.findAllByTripAndTrafficMode(tripEntity, TripTrafficModeMapper.map(tripProfile.getTrafficMode()));

            stopTimeCommandService.deleteAllByTripProfile(tripProfileEntity);

            StopTime[] stopTimes = tripProfile.getStops().toArray(StopTime[]::new);

            List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                    .mapToObj(i -> {
                        StopTime stopTime = stopTimes[i];

                        StopTimeEntity entity = new StopTimeEntity();

                        StopTimeId stopTimeId = new StopTimeId();
                        stopTimeId.setTripProfileId(tripProfileEntity.getTripProfileId());
                        stopTimeId.setStopSequence(i + 1);
                        entity.setStopTimeId(stopTimeId);
                        entity.setTripProfile(tripProfileEntity);

                        entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
                        entity.setDistanceMeters(stopTime.getMeters());
                        entity.setCalculatedTimeSeconds(stopTime.getCalculatedSeconds());
                        if (tripProfile.getIsCustomized()) {
                            entity.setCustomizedTimeSeconds(stopTime.getCustomizedSeconds());
                        } else {
                            entity.setCustomizedTimeSeconds(stopTime.getCalculatedSeconds());
                        }

                        return entity;
                    }).toList();

            stopTimeCommandService.saveAll(entities);
        }

        tripCommandService.save(updatedTrip);

        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

}
