package pl.wrona.webserver.bussiness.calendar;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarItemEntity;
import pl.wrona.webserver.core.calendar.CalendarItemQueryRepository;

@Service
@AllArgsConstructor
public class CalendarItemCommandService {

    private final CalendarItemQueryRepository calendarItemQueryRepository;

    @Transactional
    public CalendarItemEntity save(CalendarItemEntity calendarItemEntity) {
        return calendarItemQueryRepository.save(calendarItemEntity);
    }

}
