package pl.wrona.webserver.bussiness.brigade.resource;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.brigade.BrigadeResourceSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeResourceSequenceQueryService {

    private final BrigadeResourceSequenceQueryRepository brigadeResourceSequenceQueryRepository;
    private final BrigadeResourceSequenceCommandService brigadeResourceSequenceCommandService;

    @Transactional
    public Integer findNextValue(String agencyCode, String brigadeCode, Integer calendarItemSequence, String calendarSymbol) {
        var brigadeItemSequence = Hex.fromHex(brigadeCode);
        var sequencer = brigadeResourceSequenceQueryRepository.findByAgencyCodeAndBrigadeItemSequenceAndCalendarItemSequenceAndCalendarSymbol(
                agencyCode, brigadeItemSequence, calendarItemSequence, calendarSymbol);
        if (sequencer == null) {
            sequencer = brigadeResourceSequenceCommandService.init(agencyCode, brigadeItemSequence, calendarItemSequence, calendarSymbol);
        }
        var nextValue = sequencer.getNextValue();
        sequencer.setNextValue(nextValue + 1);
        brigadeResourceSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
