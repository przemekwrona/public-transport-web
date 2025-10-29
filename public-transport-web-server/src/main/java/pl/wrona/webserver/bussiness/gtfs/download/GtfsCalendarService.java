package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class GtfsCalendarService {

    private final GtfsCalendarRepository gtfsCalendarRepository;
    private final GtfsCalendarDateRepository gtfsCalendarDateRepository;


    public List<CalendarEntity> findAllActiveCalendarDate(String instance) {
        LocalDate now = LocalDate.now();

        return this.gtfsCalendarRepository.findAllByAgencyAndStartDateGreaterThanEqual(instance, now);
    }

    public List<CalendarDatesEntity> findAllActiveCalendarDate(List<CalendarEntity> calendars) {
        return this.gtfsCalendarDateRepository.findAllByCalendarIn(calendars);
    }
}
