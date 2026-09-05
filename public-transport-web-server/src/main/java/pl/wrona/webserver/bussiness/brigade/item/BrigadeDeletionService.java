package pl.wrona.webserver.bussiness.brigade.item;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupCommandService;
import pl.wrona.webserver.bussiness.brigade.group.BrigadeGroupQueryService;
import pl.wrona.webserver.bussiness.brigade.resource.deletion.ResourceDeletionService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeDeletionService {

    private final BrigadeGroupQueryService brigadeGroupQueryService;
    private final ResourceDeletionService resourceDeletionService;
    private final BrigadeGroupCommandService brigadeGroupCommandService;
    private final BrigadeItemQueryService brigadeItemQueryService;
    private final BrigadeItemCommandService brigadeItemCommandService;

    @Transactional
    @PreAgencyAuthorize()
    public Status deleteBrigadeByBrigadeCode(String agency, String brigadeCode) {
        var brigadeGroups = brigadeGroupQueryService.findAllByBrigadeCode(agency, brigadeCode);
        for (var brigadeGroup : brigadeGroups) {
            var calendarCode = brigadeGroup.getCalendarSymbol().getCalendarItem().getSequenceHex();
            var symbol = brigadeGroup.getCalendarSymbol().getDesignation();
            resourceDeletionService.deleteResource(agency, brigadeCode, calendarCode, symbol);
        }

        if (!brigadeGroups.isEmpty()) {
            brigadeGroupCommandService.deleteAll(brigadeGroups);
        }

        var brigadeItem = brigadeItemQueryService.findByBrigadeCode(agency, brigadeCode);
        if (brigadeItem != null) {
            brigadeItemCommandService.delete(brigadeItem);
        }

        return new Status().status(Status.StatusEnum.DELETED);
    }
}
