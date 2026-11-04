package pl.wrona.webserver.stops;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.CenterPoint;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.Stop;
import org.igeolab.iot.pt.server.api.model.StopsPatchRequest;
import org.igeolab.iot.pt.server.api.model.StopsResponse;
import org.igeolab.iot.pt.server.api.model.Territory;
import org.igeolab.iot.pt.server.api.model.Teryt;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.stop.name.AgencyStopNameService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.TerritorialUnitQueryService;
import pl.wrona.webserver.core.TerytQueryService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.StopAgencyRepository;
import pl.wrona.webserver.core.entity.TerritorialUnitEntity;
import pl.wrona.webserver.core.entity.TerytEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BusStopService {

    private final BusStopRepository busStopRepository;
    private final AgencyService agencyService;
    private final TerritorialUnitQueryService territorialUnitQueryService;
    private final TerytQueryService terytQueryService;
    private final AgencyStopNameService agencyStopNameService;
    private final StopAgencyRepository stopAgencyRepository;

    public StopsResponse findBusStop(float maxLat, float minLon, float minLat, float maxLon) {
        var stopEntities = busStopRepository.findAllByArea(maxLat, minLon, minLat, maxLon);
        var territorialUnitIds = stopEntities.stream().map(BusStop::getTerritorialUnit).filter(Objects::nonNull).map(TerritorialUnitEntity::getTerritorialUnitId).toList();
        var territoryEntities = territorialUnitQueryService.findAllByTerritoriesIdIn(territorialUnitIds);
        var tertitoryIdterc = territoryEntities.stream().map(TerritorialUnitEntity::getIdterc).collect(Collectors.toSet());
        var tertitoryTeryts = territoryEntities.stream().map(TerritorialUnitEntity::getTeryt).collect(Collectors.toSet());

        var gminy = terytQueryService.findTerrytGminaByIdtercIn(tertitoryIdterc);
        var powiaty = terytQueryService.findTerrytPowiatByTeryIn(tertitoryTeryts);
        var wojewodztwa = terytQueryService.findTerrytWojewodztwoByWojIn(tertitoryTeryts).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(TerytEntity::getWoj, Function.identity()));

        var teryts = powiaty.stream()
                .map(it -> new Teryt()
                        .teryt(it.getTeryt())
                        .terytName(it.getNazwa())
                        .voivodeshipTeryt(wojewodztwa.get(it.getWoj()).getTeryt())
                        .voivodeshipName(wojewodztwa.get(it.getWoj()).getNazwa()))
                .collect(Collectors.toMap(Teryt::getTeryt, Function.identity()));

        var territories = territoryEntities.stream()
                .map(it -> new Territory()
                        .id(it.getLokalnyid())
                        .teryt(teryts.get(it.getTeryt()))
                        .idterc(it.getIdterc())
                        .name(it.getNazwa())
                        .type(it.getRodzaj()))
                .toList();

        List<Stop> stops = stopEntities.stream()
                .filter(Objects::nonNull)
                .map(BusStopMapper::apply)
                .collect(Collectors.toCollection(ArrayList::new));

        return new StopsResponse()
                .stops(stops)
                .territories(territories);
    }

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public StopsResponse findAgencyStopsInArea(String instance, float maxLat, float minLon, float minLat, float maxLon) {
        StopsResponse response = findBusStop(maxLat, minLon, minLat, maxLon);
        applyAgencyNames(instance, response.getStops());
        return response;
    }

    public StopsResponse findStopsByStopName(String stopName) {
        List<Stop> stops = busStopRepository.findBusStopByNameStartsWith(stopName).stream()
                .map(BusStopMapper::apply)
                .sorted(Comparator.comparingInt(stop -> stop.getName() == null ? 0 : stop.getName().length()))
                .toList();

        return new StopsResponse()
                .stops(stops);
    }

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    public StopsResponse findAgencyStopsByStopName(String instance, String stopName) {
        List<BusStop> stopEntities = new ArrayList<>(busStopRepository.findBusStopByNameStartsWith(stopName));
        Set<Long> known = stopEntities.stream().map(BusStop::getStopId).collect(Collectors.toCollection(HashSet::new));
        List<Long> extraIds = stopAgencyRepository.findByAgencyCodeAndNameStartingWith(instance, stopName).stream()
                .map(row -> row.getStop().getStopId())
                .filter(id -> id != null && !known.contains(id))
                .toList();
        if (!extraIds.isEmpty()) {
            stopEntities.addAll(busStopRepository.findAllById(extraIds));
        }

        List<Stop> stops = stopEntities.stream()
                .filter(Objects::nonNull)
                .map(BusStopMapper::apply)
                .collect(Collectors.toCollection(ArrayList::new));
        applyAgencyNames(instance, stops);
        stops.sort(Comparator.comparingInt(stop -> stop.getName() == null ? 0 : stop.getName().length()));

        return new StopsResponse()
                .stops(stops);
    }

    public Status patchStop(StopsPatchRequest stopsPatchRequest) {
        busStopRepository.findById(stopsPatchRequest.getId()).ifPresent(busStop -> {
            if (stopsPatchRequest.getActive() != null) {
                busStop.setActive(stopsPatchRequest.getActive());
            }
            busStopRepository.save(busStop);
        });
        return new Status()
                .status(Status.StatusEnum.SUCCESS);
    }

    private void applyAgencyNames(String agency, List<Stop> stops) {
        if (agency == null || agency.isBlank() || stops == null || stops.isEmpty()) {
            return;
        }
        Map<Long, String> overrides = agencyStopNameService.findOverrides(agency, stops.stream().map(Stop::getId).toList());
        agencyStopNameService.apply(overrides, stops.toArray(Stop[]::new));
    }

    public CenterPoint centerMap() {
        AgencyEntity agencyEntity = agencyService.getLoggedAgency();
        return new CenterPoint()
                .latitude(agencyEntity.getLatitude())
                .longitude(agencyEntity.getLongitude())
                .zoom(14);
    }
}
