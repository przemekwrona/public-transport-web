package pl.wrona.webserver.bussiness.trip.profile;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripProfile;
import org.igeolab.iot.pt.server.api.model.UpdateTripProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
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
import pl.wrona.webserver.core.agency.TripTrafficMode;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripProfileMapper;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class TripProfileUpdaterService {

    private final TripQueryService tripQueryService;
    private final StopTimeCommandService stopTimeCommandService;
    private final StopService stopService;
    private final TripCommandService tripCommandService;
    private final TripProfileQueryService tripProfileQueryService;
    private final TripProfileCommandService tripProfileCommandService;
    private final BrigadeEventQueryService brigadeEventQueryService;

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER
    })
    public Status updateTripProfile(String instance, String routeCode, String tripCode, UpdateTripProfileRequest request) {
        TripEntity tripEntity = requireTrip(instance, routeCode, tripCode);
        List<TripProfile> tripProfiles = profilesOf(request);
        saveProfiles(tripEntity, tripProfiles);
        finishTrip(tripEntity, tripProfiles, request);
        return success();
    }

    @Transactional
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER
    })
    public Status updateTripProfiles(String instance, String routeCode, String tripCode, UpdateTripProfileRequest request) {
        TripEntity tripEntity = requireTrip(instance, routeCode, tripCode);
        List<TripProfile> tripProfiles = profilesOf(request);
        if (tripProfiles.isEmpty()) {
            throw new BusinessException("ERROR:202611091054", "Trip profile list cannot be empty");
        }
        if (tripProfiles.stream().map(TripProfile::getTrafficMode).distinct().count() != tripProfiles.size()) {
            throw new BusinessException("ERROR:202611091053", "Trip profile list contains a duplicate traffic mode");
        }

        saveProfiles(tripEntity, tripProfiles);
        removeProfilesMissingFrom(tripEntity, tripProfiles);
        finishTrip(tripEntity, tripProfiles, request);
        return success();
    }

    private TripEntity requireTrip(String instance, String routeCode, String tripCode) {
        TripEntity tripEntity = tripQueryService.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);
        if (tripEntity == null) {
            throw new BusinessException("ERROR:202611091032", "Trip " + tripCode + " does not exist");
        }
        return tripEntity;
    }

    private void saveProfiles(TripEntity tripEntity, List<TripProfile> tripProfiles) {
        Map<Long, StopEntity> stopDictionary = stopService.mapStopByIdsIn(stopIds(tripProfiles));

        for (TripProfile tripProfile : tripProfiles) {
            if (tripProfile.getStops() == null || tripProfile.getStops().isEmpty()) {
                throw new BusinessException("ERROR:202611091033", "Trip profile requires a stop list");
            }

            TripTrafficMode trafficMode = TripTrafficModeMapper.map(tripProfile.getTrafficMode());
            TripProfileEntity existedTripProfileEntity = tripProfileQueryService.findAllByTripAndTrafficMode(tripEntity, trafficMode);

            if (existedTripProfileEntity == null) {
                TripProfileEntity updatedTripProfile = TripProfileMapper.map(tripProfile);
                updatedTripProfile.setTrip(tripEntity);
                tripProfileCommandService.save(updatedTripProfile);
            } else {
                TripProfileEntity updatedTripProfile = TripProfileMapper.update(existedTripProfileEntity, tripProfile);
                tripProfileCommandService.save(updatedTripProfile);
            }

            TripProfileEntity tripProfileEntity = tripProfileQueryService.findAllByTripAndTrafficMode(tripEntity, trafficMode);
            stopTimeCommandService.deleteAllByTripProfile(tripProfileEntity);

            StopTime[] stopTimes = tripProfile.getStops().toArray(StopTime[]::new);
            List<StopTimeEntity> entities = IntStream.range(0, stopTimes.length)
                    .mapToObj(i -> stopTime(tripProfile, tripProfileEntity, stopDictionary, stopTimes[i], i + 1))
                    .toList();

            stopTimeCommandService.saveAll(entities);
        }
    }

    private void removeProfilesMissingFrom(TripEntity tripEntity, List<TripProfile> tripProfiles) {
        Set<TripTrafficMode> kept = tripProfiles.stream()
                .map(profile -> TripTrafficModeMapper.map(profile.getTrafficMode()))
                .collect(Collectors.toSet());

        for (TripProfileEntity existing : tripProfileQueryService.findAllByTrip(tripEntity)) {
            if (kept.contains(existing.getTrafficMode())) {
                continue;
            }
            if (brigadeEventQueryService.existsByTripProfile(existing)) {
                throw new BusinessException("ERROR:202611091052", "Profile " + existing.getTrafficMode() + " is used by a brigade");
            }
            existing.setStopTimes(new HashSet<>());
            stopTimeCommandService.deleteAllByTripProfile(existing);
            tripProfileCommandService.delete(existing);
        }
    }

    private void finishTrip(TripEntity tripEntity, List<TripProfile> tripProfiles, UpdateTripProfileRequest request) {
        applyDistance(tripEntity, tripProfiles);
        TripMapper.applyGeometry(tripEntity, request.getGeometry());
        tripCommandService.save(tripEntity);
    }

    private static Status success() {
        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    private static List<TripProfile> profilesOf(UpdateTripProfileRequest request) {
        return request.getTripProfiles() == null ? List.of() : request.getTripProfiles();
    }

    private static List<Long> stopIds(List<TripProfile> tripProfiles) {
        return tripProfiles.stream()
                .filter(profile -> profile.getStops() != null)
                .flatMap(profile -> profile.getStops().stream())
                .map(StopTime::getStopId)
                .filter(Objects::nonNull)
                .toList();
    }

    private static StopTimeEntity stopTime(TripProfile tripProfile, TripProfileEntity tripProfileEntity, Map<Long, StopEntity> stopDictionary, StopTime stopTime, int stopSequence) {
        StopTimeEntity entity = new StopTimeEntity();

        StopTimeId stopTimeId = new StopTimeId();
        stopTimeId.setTripProfileId(tripProfileEntity.getTripProfileId());
        stopTimeId.setStopSequence(stopSequence);
        entity.setStopTimeId(stopTimeId);
        entity.setTripProfile(tripProfileEntity);
        entity.setStopEntity(stopDictionary.get(stopTime.getStopId()));
        entity.setDistanceMeters(stopTime.getMeters());
        entity.setCalculatedTimeSeconds(stopTime.getCalculatedSeconds());
        if (Boolean.TRUE.equals(tripProfile.getIsCustomized())) {
            entity.setCustomizedTimeSeconds(stopTime.getCustomizedSeconds());
        } else {
            entity.setCustomizedTimeSeconds(stopTime.getCalculatedSeconds());
        }
        return entity;
    }

    private static void applyDistance(TripEntity tripEntity, List<TripProfile> tripProfiles) {
        if (tripProfiles.isEmpty()) {
            return;
        }
        TripProfile source = tripProfiles.stream()
                .filter(profile -> Boolean.TRUE.equals(profile.getIsDefault()))
                .findFirst()
                .orElse(tripProfiles.get(0));
        StopTime lastStop = source.getStops().get(source.getStops().size() - 1);
        if (lastStop.getMeters() != null) {
            tripEntity.setDistanceInMeters(lastStop.getMeters());
        }
    }
}
