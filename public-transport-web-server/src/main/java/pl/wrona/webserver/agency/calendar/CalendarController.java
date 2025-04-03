package pl.wrona.webserver.agency.calendar;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
}
