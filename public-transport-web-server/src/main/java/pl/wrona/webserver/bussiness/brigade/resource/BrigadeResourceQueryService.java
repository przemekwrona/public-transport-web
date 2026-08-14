package pl.wrona.webserver.bussiness.brigade.resource;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeResourceQueryService {

    private final BrigadeResourceQueryRepository brigadeResourceQueryRepository;

    public BrigadeResourceEntity findById(Long brigadeResourceId) {
        return brigadeResourceQueryRepository.findById(brigadeResourceId).orElse(null);
    }

}
