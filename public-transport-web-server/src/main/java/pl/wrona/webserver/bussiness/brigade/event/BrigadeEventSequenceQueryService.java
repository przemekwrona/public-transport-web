package pl.wrona.webserver.bussiness.brigade.event;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeEventSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeEventSequenceQueryService {

    private final BrigadeEventSequenceQueryRepository brigadeEventSequenceQueryRepository;
    private final BrigadeEventSequenceCommandService brigadeEventSequenceCommandService;

    @Transactional
    public Integer findNextValue(String agencyCode, Integer brigadeItemSequence, String calendarCode, String calendarSymbol) {
        var sequencer = brigadeEventSequenceQueryRepository.findByAgencyCodeAndBrigadeItemSequenceAndCalendarCodeAndCalendarSymbol(
                agencyCode, brigadeItemSequence, calendarCode, calendarSymbol);
        if (sequencer == null) {
            sequencer = brigadeEventSequenceCommandService.init(agencyCode, brigadeItemSequence, calendarCode, calendarSymbol);
        }
        var nextValue = sequencer.getNextValue().intValue();
        sequencer.setNextValue(sequencer.getNextValue() + 1);
        brigadeEventSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
