package pl.wrona.webserver.bussiness.brigade.optimizer;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.lucene.util.SloppyMath;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.optimizer.OrToolsBrigadeOptimizer.TripRequest;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.stop.FirstAndLastStop;
import pl.wrona.webserver.bussiness.stop.StopQueryService;
import pl.wrona.webserver.bussiness.trip.measure.TripDistanceMeasureService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.TripProfileEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrToolsResourceOptimizerService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceQueryService brigadeResourceQueryService;
    private final BrigadeEventQueryService brigadeEventQueryService;
    private final StopQueryService stopQueryService;

    @PreAgencyAuthorize
    @Transactional
    public void optimizeBrigades(String instance, String brigadeCode, String calendarCode, String symbol) {
        var brigadeGroup = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode, calendarCode, symbol);
        if (brigadeGroup == null) {
            return;
        }

        var resources = brigadeResourceQueryService.findByBrigadeGroup(brigadeGroup).stream()
                .sorted(Comparator.comparing(BrigadeResourceEntity::getResourceSequence, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        var resourceIds = resources.stream()
                .map(BrigadeResourceEntity::getBrigadeResourceId)
                .toList();
        var events = brigadeEventQueryService.findAllByResourceIds(resourceIds);

        if (resources.isEmpty() || events.isEmpty()) {
            return;
        }

        var profiles = events.stream().map(BrigadeEventEntity::getTripProfile).toList();
        var edgeStopsByProfile = stopQueryService.findFirstAndLastStops(profiles);

    }
}
