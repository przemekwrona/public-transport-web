package pl.wrona.webserver.bussiness.trip.details;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.StopTime;
import org.igeolab.iot.pt.server.api.model.TripId;
import org.igeolab.iot.pt.server.api.model.TripsDetails;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.mapper.TripMapper;
import pl.wrona.webserver.core.mapper.TripModeMapper;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TripDetailsService {

    private final TripQueryService tripQueryService;
    private final StopTimeRepository stopTimeRepository;
    private final ObjectMapper objectMapper;

    @PreAgencyAuthorize
    public TripsDetails getTripByTripId(String instance, TripId tripId) {
        var tripEntity = tripQueryService.findByAgencyCodeAndTripId(instance, tripId);

        var tripResponse = Optional.ofNullable(tripEntity)
                .map(trip -> TripMapper.map(trip, Map.of()))
                .orElse(null);

        List<StopTimeEntity> stopTimes = tripEntity != null
                ? stopTimeRepository.findAllByTripId(tripEntity.getTripId()) : List.of();

        List<StopTime> stops = stopTimes.stream().map((StopTimeEntity stopTime) -> new StopTime()
                .stopId(stopTime.getStopEntity().getStopId())
                .stopName(stopTime.getStopEntity().getName())
                .calculatedSeconds(stopTime.getCalculatedTimeSeconds())
                .customizedSeconds(stopTime.getCustomizedTimeSeconds())
                .meters(stopTime.getDistanceMeters())
                .bdot10k(stopTime.getStopEntity().isBdot10k())
                .lon((float) stopTime.getStopEntity().getLon())
                .lat((float) stopTime.getStopEntity().getLat())).toList();

        List<Point2D> geometry = new ArrayList<>();

        if (tripEntity.getGeometry() != null) {
            try {
                geometry = objectMapper.readValue(tripEntity.getGeometry(), new TypeReference<List<List<Float>>>() {
                        }).stream()
                        .map((List<Float> point) -> new Point2D()
                                .lat(point.get(0))
                                .lon(point.get(1)))
                        .toList();
            } catch (Exception exception) {
                throw new BusinessException("ERROR:202511031624", exception.getMessage());
            }
        }

        return new TripsDetails()
                .stops(stops)
                .geometry(geometry)
                .isCustomized(tripEntity.isCustomized())
                .item(tripResponse);
    }

}
