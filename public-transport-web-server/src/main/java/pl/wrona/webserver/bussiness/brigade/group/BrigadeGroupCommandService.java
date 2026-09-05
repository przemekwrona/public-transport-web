package pl.wrona.webserver.bussiness.brigade.group;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeGroupCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;

import java.util.Collection;

@Service
@AllArgsConstructor
public class BrigadeGroupCommandService {

    private final BrigadeGroupCommandRepository brigadeGroupCommandRepository;

    @Transactional
    public void deleteAll(Collection<BrigadeGroupEntity> brigadeGroupEntities) {
        brigadeGroupCommandRepository.deleteAll(brigadeGroupEntities);
        brigadeGroupCommandRepository.flush();
    }

}
