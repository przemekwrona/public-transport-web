package pl.wrona.webserver.bussiness.brigade;


import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.BrigadeTimetableApi;
import org.igeolab.iot.pt.server.api.model.GetTimetableByBrigadeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.brigade.timetable.details.BrigadeTimetableDetailsService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class BrigadeTimetableController implements BrigadeTimetableApi {

    private final BrigadeTimetableDetailsService brigadeTimetableDetailsService;

    @Override
    public ResponseEntity<GetTimetableByBrigadeResponse> getTimetableByBrigadeAndCalendarSymbol(String agency, String brigadeCode, String calendarCode, String calendarSymbol) {
        return ResponseEntity.ok(brigadeTimetableDetailsService.getTimetableByBrigadeAndCalendarSymbol(agency, brigadeCode, calendarCode, calendarSymbol));
    }
}
