package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class GtfsCalendarService {

    private final GtfsCalendarRepository gtfsCalendarRepository;


    public List<CalendarEntity> getActiveCalendars(String instance) {
        LocalDate now = LocalDate.now();

        return this.gtfsCalendarRepository.findAllByAgencyAndStartDateGreaterThanEqual(instance, now);
    }
}
