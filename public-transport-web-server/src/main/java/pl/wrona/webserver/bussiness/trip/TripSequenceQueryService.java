package pl.wrona.webserver.bussiness.trip;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripSequenceQueryRepository;

@Service
@AllArgsConstructor
public class TripSequenceQueryService {

    private final TripSequenceQueryRepository tripSequenceQueryRepository;
    private final TripSequenceCommandService tripSequenceCommandService;

    @Transactional
    public Integer findNextValue(String agencyCode, Integer routeSequence) {
        var sequencer = tripSequenceQueryRepository.findByAgencyCodeAndRouteSequence(agencyCode, routeSequence);
        if (sequencer == null) {
            sequencer = tripSequenceCommandService.init(agencyCode, routeSequence);
        }
        var nextValue = sequencer.getNextValue().intValue();
        sequencer.setNextValue(sequencer.getNextValue() + 1);
        tripSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
