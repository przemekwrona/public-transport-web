package pl.wrona.webserver.bussiness.route;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.RouteSequenceQueryRepository;

@Service
@AllArgsConstructor
public class RouteSequenceQueryService {

    private final RouteSequenceQueryRepository routeSequenceQueryRepository;
    private final RouteSequenceCommandService routeSequenceCommandService;

    @Transactional
    public Integer findNextValue(String agencyCode) {
        var sequencer = routeSequenceQueryRepository.findByAgencyCode(agencyCode);
        if (sequencer == null) {
            sequencer = routeSequenceCommandService.init(agencyCode);
        }
        var nextValue = sequencer.getNextValue().intValue();
        sequencer.setNextValue(sequencer.getNextValue() + 1);
        routeSequenceQueryRepository.save(sequencer);
        return nextValue;
    }

}
