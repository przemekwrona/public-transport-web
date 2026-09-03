package pl.wrona.webserver.bussiness.brigade.item;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemQueryRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeItemQueryService {

    private final BrigadeItemQueryRepository brigadeItemQueryRepository;

    public List<BrigadeItemEntity> findAll(String instance) {
        return brigadeItemQueryRepository.findAllByAgencyCode(instance);
    }

    public BrigadeItemEntity findByBrigadeCode(String instance, String brigadeCode) {
        return brigadeItemQueryRepository.findByAgencyCodeAndSequenceHex(instance, brigadeCode);
    }

}
