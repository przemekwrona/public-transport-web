package pl.wrona.webserver.bussiness.brigade.item.creator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.CreateBrigadeBody;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemCommandService;
import pl.wrona.webserver.bussiness.brigade.item.BrigadeItemSequenceQueryService;
import pl.wrona.webserver.bussiness.calendar.CalendarItemQueryService;
import pl.wrona.webserver.bussiness.route.RouteQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeItemCreatorService {

    private final AgencyService agencyService;
    private final CalendarItemQueryService calendarItemQueryService;
    private final BrigadeItemSequenceQueryService brigadeItemSequenceQueryService;
    private final BrigadeItemCommandService brigadeItemCommandService;
    private final RouteQueryService routeQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status createBrigade(String instance, CreateBrigadeBody request) {

        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);

        var routeCode = request.getSelectedRouteCode();
        var routeEntity = routeQueryService.findRouteByAgencyCodeAndRouteCode(agencyEntity.getAgencyCode(), routeCode);

        var calendarCode = request.getCalendarCode();
        var calendarItem = calendarItemQueryService.findByAgencyCalendarCode(instance, calendarCode);

        var brigadeItemEntity = new BrigadeItemEntity();
        brigadeItemEntity.setName(request.getBrigadeName());
        brigadeItemEntity.setAgency(agencyEntity);
        brigadeItemEntity.setCalendarItem(calendarItem);
        brigadeItemEntity.setDefaultRoute(routeEntity);

        var nextSequence = brigadeItemSequenceQueryService.findNextValue(instance, calendarCode);
        brigadeItemEntity.setSequence(nextSequence);
        brigadeItemEntity.setSequenceHex(Hex.toHex(nextSequence));

        var savedBrigade = brigadeItemCommandService.save(brigadeItemEntity);

        return new Status().status(Status.StatusEnum.CREATED);
    }

}
