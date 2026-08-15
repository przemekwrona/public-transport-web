package pl.wrona.webserver.bussiness.brigade.item;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeItemSequenceCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeItemSequenceEntity;
import pl.wrona.webserver.core.brigade.BrigadeItemSequenceId;
import pl.wrona.webserver.core.brigade.BrigadeItemSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeItemSequenceCommandService {

    private final BrigadeItemSequenceCommandRepository brigadeItemSequenceCommandRepository;
    private final BrigadeItemSequenceQueryRepository brigadeItemSequenceQueryRepository;

    @Transactional
    public BrigadeItemSequenceEntity init(String agencyCode, String calendarCode) {
        var existing = brigadeItemSequenceQueryRepository.findByAgencyCodeAndCalendarCode(agencyCode, calendarCode);
        if (existing != null) {
            return existing;
        }
        return brigadeItemSequenceCommandRepository.save(new BrigadeItemSequenceEntity(
                new BrigadeItemSequenceId(agencyCode, calendarCode), 1L));
    }

}
