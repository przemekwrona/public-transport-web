package pl.wrona.webserver.bussiness.calendar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wrona.webserver.core.calendar.CalendarSequenceEntity;
import pl.wrona.webserver.core.calendar.CalendarSequenceQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarSequenceQueryServiceTest {

    @Mock
    private CalendarSequenceQueryRepository calendarSequenceQueryRepository;
    @Mock
    private CalendarSequenceCommandService calendarSequenceCommandService;

    @InjectMocks
    private CalendarSequenceQueryService calendarSequenceQueryService;

    @Test
    void incrementsCounterAfterInit() {
        var sequencer = new CalendarSequenceEntity("NEXTSTOP", 1);
        when(calendarSequenceQueryRepository.findByAgencyCode("NEXTSTOP")).thenReturn(null);
        when(calendarSequenceCommandService.init("NEXTSTOP")).thenReturn(sequencer);

        Integer nextValue = calendarSequenceQueryService.findByAgencyCode("NEXTSTOP");

        assertThat(nextValue).isEqualTo(1);
        assertThat(sequencer.getNextValue()).isEqualTo(2);
        verify(calendarSequenceQueryRepository).save(sequencer);
    }

    @Test
    void incrementsExistingCounter() {
        var sequencer = new CalendarSequenceEntity("NEXTSTOP", 2);
        when(calendarSequenceQueryRepository.findByAgencyCode("NEXTSTOP")).thenReturn(sequencer);

        Integer nextValue = calendarSequenceQueryService.findByAgencyCode("NEXTSTOP");

        assertThat(nextValue).isEqualTo(2);
        assertThat(sequencer.getNextValue()).isEqualTo(3);
        verify(calendarSequenceQueryRepository).save(sequencer);
    }
}
