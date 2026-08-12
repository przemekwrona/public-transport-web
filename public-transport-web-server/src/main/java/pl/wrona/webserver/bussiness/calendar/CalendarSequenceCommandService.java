package pl.wrona.webserver.bussiness.calendar;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarSequenceCommandRepository;
import pl.wrona.webserver.core.calendar.CalendarSequenceEntity;
import pl.wrona.webserver.core.calendar.CalendarSequenceQueryRepository;

@Service
@AllArgsConstructor
public class CalendarSequenceCommandService {

    private final CalendarSequenceCommandRepository calendarSequenceCommandRepository;
    private final CalendarSequenceQueryRepository calendarSequenceQueryRepository;

    @Transactional
    public CalendarSequenceEntity save(CalendarSequenceEntity entity) {
        return calendarSequenceCommandRepository.save(entity);
    }

    @Transactional
    public CalendarSequenceEntity init(String agencyCode) {
        var existing = calendarSequenceQueryRepository.findByAgencyCode(agencyCode);
        if (existing != null) {
            return existing;
        }
        return calendarSequenceCommandRepository.save(new CalendarSequenceEntity(agencyCode, 1));
    }

}
