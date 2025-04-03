package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CalendarService {

    private final CalendarRepository calendarRepository;
}
