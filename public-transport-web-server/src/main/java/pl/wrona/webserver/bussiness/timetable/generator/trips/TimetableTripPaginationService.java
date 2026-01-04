package pl.wrona.webserver.bussiness.timetable.generator.trips;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.TimetableTrip;
import org.igeolab.iot.pt.server.api.model.TripFilter;
import org.igeolab.iot.pt.server.api.model.TripId1;
import org.igeolab.iot.pt.server.api.model.TripResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripVariantMode;
import pl.wrona.webserver.core.mapper.TripTrafficModeMapper;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TimetableTripPaginationService {

    private final TripQueryService tripQueryService;

    @PreAgencyAuthorize
    public TripResponse findTrips(String instance, TripFilter tripFilter) {
        var trips = tripQueryService.findByAgencyCodeAndLineAndName(instance, tripFilter.getRouteId().getLine(), tripFilter.getRouteId().getName());

        var frontTrips = trips.stream()
                .filter(trip -> trip.getVariantMode().equals(TripVariantMode.FRONT))
                .map(TimetableTripPaginationService::mapTripEntity)
                .toList();

        var mainTripFront = trips.stream()
                .filter(TripEntity::isMainVariant)
                .filter(trip -> trip.getVariantMode().equals(TripVariantMode.FRONT))
                .map(TimetableTripPaginationService::mapTripEntity)
                .findFirst().orElse(null);

        var backTrips = trips.stream()
                .filter(trip -> trip.getVariantMode().equals(TripVariantMode.BACK))
                .map(TimetableTripPaginationService::mapTripEntity)
                .toList();

        var mainTripBack = trips.stream()
                .filter(TripEntity::isMainVariant)
                .filter(trip -> trip.getVariantMode().equals(TripVariantMode.BACK))
                .map(TimetableTripPaginationService::mapTripEntity)
                .findFirst().orElse(null);

        return new TripResponse()
                .routeFilter(tripFilter.getRouteId())
                .front(frontTrips)
                .back(backTrips);
    }

    private static TimetableTrip mapTripEntity(TripEntity trip) {
        return new TimetableTrip()
                .tripId(new TripId1()
                        .routeId(new RouteId()
                                .line(trip.getRoute().getLine())
                                .name(trip.getRoute().getName()))
                        .variantName(trip.getVariantName())
                        .variantMode(TripVariantModeMapper.map(trip.getVariantMode()))
                        .trafficMode(TripTrafficModeMapper.map(trip.getTrafficMode())))
                .designation(trip.getVariantDesignation())
                .description(trip.getVariantDescription());
    }
}
