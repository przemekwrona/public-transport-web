package pl.wrona.webserver.bussiness.brigade.optimizer;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.stop.StopQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

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

        var resources = brigadeResourceQueryService.findByBrigadeGroup(brigadeGroup);
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
