package pl.wrona.webserver.bussiness.brigade.timetable.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetTimetableByBrigadeResponse;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BrigadeTimetableDetailsService {

    public GetTimetableByBrigadeResponse getTimetableByBrigadeAndCalendarSymbol(String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        return null;
    }
}
