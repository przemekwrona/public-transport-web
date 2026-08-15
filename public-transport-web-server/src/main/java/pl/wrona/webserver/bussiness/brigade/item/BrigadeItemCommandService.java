package pl.wrona.webserver.bussiness.brigade.item;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeItemCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeItemEntity;

@Service
@AllArgsConstructor
public class BrigadeItemCommandService {

    private final BrigadeItemCommandRepository brigadeItemCommandRepository;

    @Transactional
    public BrigadeItemEntity save(BrigadeItemEntity brigadeItemEntity) {
        return brigadeItemCommandRepository.save(brigadeItemEntity);
    }

}
