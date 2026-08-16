package pl.wrona.webserver.bussiness.brigade.timetable.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetTimetableByBrigadeResponse;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.brigade.event.BrigadeEventQueryService;
import pl.wrona.webserver.core.brigade.BrigadeEventEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeTimetableDetailsService {

    private final BrigadeEventQueryService brigadeEventQueryService;

    @PreAgencyAuthorize
    public GetTimetableByBrigadeResponse getTimetableByBrigadeAndCalendarSymbol(
            String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        List<BrigadeEventEntity> events = brigadeEventQueryService.findAllByAgencyAndBrigadeAndCalendarAndSymbol(
                instance, brigadeCode, calendarCode, calendarSymbol);

        return new GetTimetableByBrigadeResponse();
    }
}
