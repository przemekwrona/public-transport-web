package pl.wrona.webserver.bussiness.brigade.item;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeItemSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeItemSequenceQueryService {

    private final BrigadeItemSequenceQueryRepository brigadeItemSequenceQueryRepository;
    private final BrigadeItemSequenceCommandService brigadeItemSequenceCommandService;

    @Transactional
    public Integer findNextValue(String agencyCode, String calendarCode) {
        var sequencer = brigadeItemSequenceQueryRepository.findByAgencyCodeAndCalendarCode(agencyCode, calendarCode);
        if (sequencer == null) {
            sequencer = brigadeItemSequenceCommandService.init(agencyCode, calendarCode);
        }
        var nextValue = sequencer.getNextValue().intValue();
        sequencer.setNextValue(sequencer.getNextValue() + 1);
        brigadeItemSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
