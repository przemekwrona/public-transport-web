package pl.wrona.webserver.bussiness.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.BrigadeDeleteBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.calendar.CalendarSymbolQueryService;
import pl.wrona.webserver.core.AgencyService;
import pl.wrona.webserver.core.brigade.BrigadeEntity;
import pl.wrona.webserver.core.brigade.BrigadeRepository;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class BrigadeQueryService {

    private final AgencyService agencyService;

    private final BrigadeRepository brigadeRepository;
    private final CalendarSymbolQueryService calendarSymbolQueryService;

    @PreAgencyAuthorize
    @Transactional
    public Status updateBrigade(String instance, BrigadePatchBody brigadePatchBody) {
        String brigadeId = brigadePatchBody.getBrigadePayload().getBrigadeName();
        var agencyEntity = agencyService.findAgencyByAgencyCode(instance);
        var calendarSymbolEntity = calendarSymbolQueryService.findByAgencyAndBrigadeAndCalendarAndSymbol(instance, "", brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getCalendarItemId().getCode(), brigadePatchBody.getBrigadeBody().getCalendarSymbolId().getSymbol());

        brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyEntity, brigadeId).ifPresent((BrigadeEntity entity) -> {
            entity.setBrigadeNumber(brigadePatchBody.getBrigadeBody().getBrigadeName());
            entity.setCalendar(calendarSymbolEntity);
            brigadeRepository.save(entity);
        });

        return new Status().status(Status.StatusEnum.SUCCESS);
    }

    @PreAgencyAuthorize
    public Status deleteBrigade(String instance, BrigadeDeleteBody brigadeDeleteBody) {
        brigadeRepository.findBrigadeEntitiesByAgencyAndBrigadeNumber(agencyService.getLoggedAgency(), brigadeDeleteBody.getBrigadeName()).ifPresent(brigadeRepository::delete);
        return new Status().status(Status.StatusEnum.DELETED);
    }
}
