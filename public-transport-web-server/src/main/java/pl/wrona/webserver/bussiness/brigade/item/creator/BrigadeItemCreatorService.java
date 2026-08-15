package pl.wrona.webserver.bussiness.brigade.item.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemCommandService;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemSequenceQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeItemCreatorService {

    private final AgencyService agencyService;
    private final CalendarItemQueryService calendarItemQueryService;
    private final BrigadeItemSequenceQueryService brigadeItemSequenceQueryService;
    private final BrigadeItemCommandService brigadeItemCommandService;

    @PreAgencyAuthorize
    @Transactional
    public Status createBrigade(String instance, BrigadeBody request) {

        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarCode = request.getCalendarSymbolId().getCalendarItemId().getCode();
        var calendarItem = calendarItemQueryService.findByAgencyCalendarCode(instance, calendarCode);

        var brigadeItemEntity = new BrigadeItemEntity();
        brigadeItemEntity.setName(request.getBrigadeName());
        brigadeItemEntity.setAgency(agencyEntity);
        brigadeItemEntity.setCalendarItem(calendarItem);

        var nextSequence = brigadeItemSequenceQueryService.findNextValue(instance, calendarCode);
        brigadeItemEntity.setSequence(nextSequence);
        brigadeItemEntity.setSequenceHex(Hex.toHex(nextSequence));

        var savedBrigade = brigadeItemCommandService.save(brigadeItemEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

}
