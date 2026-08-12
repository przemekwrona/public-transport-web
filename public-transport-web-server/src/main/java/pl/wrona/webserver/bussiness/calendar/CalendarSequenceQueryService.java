package pl.wrona.webserver.bussiness.calendar;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.calendar.CalendarSequenceQueryRepository;

@Service
@AllArgsConstructor
public class CalendarSequenceQueryService {

    private final CalendarSequenceQueryRepository calendarSequenceQueryRepository;
    private final CalendarSequenceCommandService calendarSequenceCommandService;

    @Transactional
    public Integer findByAgencyCode(String agencyCode) {
        var sequencer = calendarSequenceQueryRepository.findByAgencyCode(agencyCode);
        if (sequencer == null) {
            var savedCalendarSequencer = calendarSequenceCommandService.init(agencyCode);
            return savedCalendarSequencer.getNextValue();
        }
        var nextValue = sequencer.getNextValue();
        sequencer.setNextValue(nextValue + 1);
        calendarSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
