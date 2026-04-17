package pl.wrona.webserver.bussiness.calendar;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarItemCommandRepository;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;

@Service
@AllArgsConstructor
public class CalendarItemCommandService {

    private final CalendarItemCommandRepository calendarItemCommandRepository;

    @Transactional
    public CalendarItemEntity save(CalendarItemEntity calendarItemEntity) {
        return calendarItemCommandRepository.save(calendarItemEntity);
    }

}
