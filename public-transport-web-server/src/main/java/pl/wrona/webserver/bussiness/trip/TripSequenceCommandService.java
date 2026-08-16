package pl.wrona.webserver.bussiness.trip;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.TripSequenceCommandRepository;
import pl.wrona.webserver.core.agency.TripSequenceEntity;
import pl.wrona.webserver.core.agency.TripSequenceId;
import pl.wrona.webserver.core.agency.TripSequenceQueryRepository;

@Service
@AllArgsConstructor
public class TripSequenceCommandService {

    private final TripSequenceCommandRepository tripSequenceCommandRepository;
    private final TripSequenceQueryRepository tripSequenceQueryRepository;

    @Transactional
    public TripSequenceEntity init(String agencyCode, Integer routeSequence) {
        var existing = tripSequenceQueryRepository.findByAgencyCodeAndRouteSequence(agencyCode, routeSequence);
        if (existing != null) {
            return existing;
        }
        return tripSequenceCommandRepository.save(new TripSequenceEntity(
                new TripSequenceId(agencyCode, routeSequence), 1L));
    }

    @Transactional
    public TripSequenceEntity saveNextValue(String agencyCode, Integer routeSequence, long nextValue) {
        var existing = tripSequenceQueryRepository.findByAgencyCodeAndRouteSequence(agencyCode, routeSequence);
        if (existing == null) {
            return tripSequenceCommandRepository.save(new TripSequenceEntity(
                    new TripSequenceId(agencyCode, routeSequence), nextValue));
        }
        existing.setNextValue(nextValue);
        return tripSequenceCommandRepository.save(existing);
    }

}
