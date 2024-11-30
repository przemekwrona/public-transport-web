package pl.wrona.webserver.agency.agency;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.model.Agency;

@Service
@AllArgsConstructor
public class AgencyService {

    private final AgencyRepository agencyRepository;

    public Agency findAgencyByAgencyCode(String agencyCode) {
        return agencyRepository.findByAgencyCodeEquals(agencyCode);
    }
}
