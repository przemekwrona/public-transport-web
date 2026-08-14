package pl.wrona.webserver.bussiness.brigade.resource;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeResourceEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceQueryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeResourceQueryService {

    private final BrigadeResourceQueryRepository brigadeResourceQueryRepository;

    public BrigadeResourceEntity findById(Long brigadeResourceId) {
        return brigadeResourceQueryRepository.findById(brigadeResourceId).orElse(null);
    }

    public List<BrigadeResourceEntity> findAllByBrigadeGroupId(Long brigadeGroupId) {
        return brigadeResourceQueryRepository.findAllByBrigadeGroupBrigadeGroupIdOrderBySequenceAsc(brigadeGroupId);
    }

}
