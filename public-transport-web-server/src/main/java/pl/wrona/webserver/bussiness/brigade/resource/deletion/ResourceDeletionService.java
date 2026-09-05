package pl.wrona.webserver.bussiness.brigade.resource.deletion;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventSequenceCommandService;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventSequenceQueryService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceCommandService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceSequenceCommandService;
import pl.wrona.webserver.bussiness.brigade.resource.BrigadeResourceSequenceQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventCommandRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class ResourceDeletionService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final BrigadeResourceQueryService brigadeResourceQueryService;
    private final BrigadeResourceCommandService brigadeResourceCommandService;
    private final BrigadeResourceSequenceQueryService brigadeResourceSequenceQueryService;
    private final BrigadeResourceSequenceCommandService brigadeResourceSequenceCommandService;
    private final BrigadeEventSequenceQueryService brigadeEventSequenceQueryService;
    private final BrigadeEventSequenceCommandService brigadeEventSequenceCommandService;
    private final BrigadeEventCommandRepository brigadeEventCommandRepository;

    @Transactional
    @PreAgencyAuthorize
    public Status deleteResourceAndAppendOne(String instance, String brigadeCode, String calendarCode, String symbol) {
        var deleteStatus = deleteResource(instance, brigadeCode, calendarCode, symbol);
        brigadeResourceCommandService.init(instance, brigadeCode, calendarCode, symbol);
        return deleteStatus;
    }

    @Transactional
    @PreAgencyAuthorize
    public Status deleteResource(String instance, String brigadeCode, String calendarCode, String symbol) {
        var brigadeGroup = brigadeGroupQueryService.findByBrigadeCode(instance, brigadeCode, calendarCode, symbol);
        if (brigadeGroup == null) {
            return new Status().status(Status.StatusEnum.DELETED);
        }

        var resources = brigadeResourceQueryService.findAllByBrigadeGroupId(brigadeGroup.getBrigadeGroupId());
        if (!resources.isEmpty()) {
            brigadeEventCommandRepository.deleteAllByResourceIn(resources);
            brigadeEventCommandRepository.flush();
        }

        var resourceSequence = brigadeResourceSequenceQueryService.find(instance, brigadeCode, calendarCode, symbol);
        if (resourceSequence != null) {
            brigadeResourceSequenceCommandService.delete(resourceSequence);
        }

        var eventSequence = brigadeEventSequenceQueryService.find(instance, brigadeCode, calendarCode, symbol);
        if (eventSequence != null) {
            brigadeEventSequenceCommandService.delete(eventSequence);
        }

        if (!resources.isEmpty()) {
            brigadeResourceCommandService.deleteAll(resources);
        }

        return new Status().status(Status.StatusEnum.DELETED);
    }

}
