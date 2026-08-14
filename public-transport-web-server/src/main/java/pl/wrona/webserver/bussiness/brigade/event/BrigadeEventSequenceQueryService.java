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
    public Integer findNextValue(String agencyCode, String calendarCode, String calendarSymbol) {
        var sequencer = brigadeEventSequenceQueryRepository.findByAgencyCodeAndCalendarCodeAndCalendarSymbol(
                agencyCode, calendarCode, calendarSymbol);
        if (sequencer == null) {
            sequencer = brigadeEventSequenceCommandService.init(agencyCode, calendarCode, calendarSymbol);
        }
        var nextValue = sequencer.getNextValue().intValue();
        sequencer.setNextValue(sequencer.getNextValue() + 1);
        brigadeEventSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
