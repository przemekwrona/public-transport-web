package pl.wrona.webserver.bussiness.stop.usage;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.AgencyStop;
import org.igeolab.iot.pt.server.api.model.AgencyStopRoute;
import org.igeolab.iot.pt.server.api.model.AgencyStopTrip;
import org.igeolab.iot.pt.server.api.model.AgencyStops;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.core.StopTimeRepository;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.StopTimeEntity;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.entity.StopEntity;
import pl.wrona.webserver.core.mapper.TripVariantModeMapper;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@AllArgsConstructor
public class AgencyStopUsageService {

    private final StopTimeRepository stopTimeRepository;
    private final AgencyStopNameService agencyStopNameService;

    @Transactional(readOnly = true)
    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public AgencyStops getAgencyStops(String instance) {
        Collator polish = Collator.getInstance(Locale.forLanguageTag("pl"));
        Map<Long, List<StopTimeEntity>> stopTimesByStopId = new LinkedHashMap<>();
        for (StopTimeEntity stopTime : stopTimeRepository.findAllUsedByAgencyCode(instance)) {
            StopEntity stop = stopTime.getStopEntity();
            if (stop == null || stop.getStopId() == null) {
                continue;
            }
            stopTimesByStopId.computeIfAbsent(stop.getStopId(), ignored -> new ArrayList<>()).add(stopTime);
        }

        Map<Long, String> agencyNames = agencyStopNameService.findOverrides(instance, stopTimesByStopId.keySet());
        List<AgencyStop> items = stopTimesByStopId.values().stream()
                .map(stopTimes -> toAgencyStop(stopTimes, polish, agencyNames))
                .sorted(Comparator
                        .comparing((AgencyStop item) -> stopName(item).isBlank())
                        .thenComparing(AgencyStopUsageService::stopName, polish))
                .toList();

        return new AgencyStops().items(items);
    }

    private AgencyStop toAgencyStop(List<StopTimeEntity> stopTimes, Collator polish, Map<Long, String> agencyNames) {
        Map<Long, RouteEntity> routes = new LinkedHashMap<>();
        Map<Long, TripEntity> trips = new LinkedHashMap<>();
        for (StopTimeEntity stopTime : stopTimes) {
            if (stopTime.getTripProfile() == null || stopTime.getTripProfile().getTrip() == null) {
                continue;
            }
            TripEntity trip = stopTime.getTripProfile().getTrip();
            if (trip.getTripId() != null) {
                trips.putIfAbsent(trip.getTripId(), trip);
            }
            RouteEntity route = trip.getRoute();
            if (route != null && route.getRouteId() != null) {
                routes.putIfAbsent(route.getRouteId(), route);
            }
        }

        return new AgencyStop()
                .stop(mapStop(stopTimes.get(0).getStopEntity(), agencyNames))
                .routes(routes.values().stream().map(this::mapRoute).sorted(routeOrder(polish)).toList())
                .trips(trips.values().stream().map(this::mapTrip).sorted(tripOrder(polish)).toList());
    }

    private static String stopName(AgencyStop item) {
        if (item.getStop() == null || item.getStop().getName() == null) {
            return "";
        }
        return item.getStop().getName();
    }

    private Stop mapStop(StopEntity stop, Map<Long, String> agencyNames) {
        return new Stop()
                .id(stop.getStopId())
                .bdot10kId(stop.getBdot10kId())
                .osmId(stop.getOsmId())
                .name(AgencyStopNameService.prefer(stop.getName(), agencyNames, stop.getStopId()))
                .lon((float) stop.getLon())
                .lat((float) stop.getLat())
                .isOsm(stop.isOsm())
                .isBdot10k(stop.isBdot10k());
    }

    private AgencyStopRoute mapRoute(RouteEntity route) {
        return new AgencyStopRoute()
                .routeCode(route.getRouteCode())
                .line(route.getLine())
                .name(route.getName());
    }

    private AgencyStopTrip mapTrip(TripEntity trip) {
        RouteEntity route = trip.getRoute();
        return new AgencyStopTrip()
                .routeCode(route == null ? null : route.getRouteCode())
                .line(route == null ? null : route.getLine())
                .tripCode(trip.getTripCode())
                .variantMode(TripVariantModeMapper.map(trip.getVariantMode()))
                .headsign(trip.getHeadsign());
    }

    private static Comparator<AgencyStopRoute> routeOrder(Collator polish) {
        return Comparator
                .comparing((AgencyStopRoute route) -> route.getLine() == null ? "" : route.getLine(), polish)
                .thenComparing(route -> route.getName() == null ? "" : route.getName(), polish);
    }

    private static Comparator<AgencyStopTrip> tripOrder(Collator polish) {
        return Comparator
                .comparing((AgencyStopTrip trip) -> trip.getLine() == null ? "" : trip.getLine(), polish)
                .thenComparing(trip -> trip.getTripCode() == null ? "" : trip.getTripCode(), polish);
    }
}
