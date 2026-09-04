package pl.wrona.webserver.bussiness.brigade.event;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeEventSequenceCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeEventSequenceEntity;
import pl.wrona.webserver.core.brigade.BrigadeEventSequenceId;
import pl.wrona.webserver.core.brigade.BrigadeEventSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeEventSequenceCommandService {

    private final BrigadeEventSequenceCommandRepository brigadeEventSequenceCommandRepository;
    private final BrigadeEventSequenceQueryRepository brigadeEventSequenceQueryRepository;

    @Transactional
    public void delete(BrigadeEventSequenceEntity entity) {
        brigadeEventSequenceCommandRepository.delete(entity);
        brigadeEventSequenceCommandRepository.flush();
    }

    @Transactional
    public BrigadeEventSequenceEntity init(String agencyCode, Integer brigadeItemSequence, String calendarCode, String calendarSymbol) {
        var existing = brigadeEventSequenceQueryRepository.findByAgencyCodeAndBrigadeItemSequenceAndCalendarCodeAndCalendarSymbol(
                agencyCode, brigadeItemSequence, calendarCode, calendarSymbol);
        if (existing != null) {
            return existing;
        }
        return brigadeEventSequenceCommandRepository.save(new BrigadeEventSequenceEntity(
                new BrigadeEventSequenceId(agencyCode, brigadeItemSequence, calendarCode, calendarSymbol), 1L));
    }

}
