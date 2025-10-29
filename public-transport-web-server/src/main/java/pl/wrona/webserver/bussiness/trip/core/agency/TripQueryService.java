package pl.wrona.webserver.bussiness.trip.core.agency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TripQueryService {

    private final TripQueryRepository tripQueryRepository;

    public List<TripEntity> findByAgencyCodeAndLineAndName(String agencyCode, String line, String name) {
        return tripQueryRepository.findByAgencyCodeAndLineAndName(agencyCode, line, name);
    }
}
