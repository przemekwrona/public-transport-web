package pl.wrona.webserver.bussiness.brigade.resource;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeResourceSequenceCommandRepository;
import pl.wrona.webserver.core.brigade.BrigadeResourceSequenceEntity;
import pl.wrona.webserver.core.brigade.BrigadeResourceSequenceId;
import pl.wrona.webserver.core.brigade.BrigadeResourceSequenceQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeResourceSequenceCommandService {

    private final BrigadeResourceSequenceCommandRepository brigadeResourceSequenceCommandRepository;
    private final BrigadeResourceSequenceQueryRepository brigadeResourceSequenceQueryRepository;

    @Transactional
    public BrigadeResourceSequenceEntity save(BrigadeResourceSequenceEntity entity) {
        return brigadeResourceSequenceCommandRepository.save(entity);
    }

    @Transactional
    public void delete(BrigadeResourceSequenceEntity entity) {
        brigadeResourceSequenceCommandRepository.delete(entity);
        brigadeResourceSequenceCommandRepository.flush();
    }

    @Transactional
    public BrigadeResourceSequenceEntity init(String agencyCode, Integer brigadeItemSequence, Integer calendarItemSequence, String calendarSymbol) {
        var existing = brigadeResourceSequenceQueryRepository.findByAgencyCodeAndBrigadeItemSequenceAndCalendarItemSequenceAndCalendarSymbol(
                agencyCode, brigadeItemSequence, calendarItemSequence, calendarSymbol);
        if (existing != null) {
            return existing;
        }
        return brigadeResourceSequenceCommandRepository.save(new BrigadeResourceSequenceEntity(
                new BrigadeResourceSequenceId(agencyCode, brigadeItemSequence, calendarItemSequence, calendarSymbol), 1));
    }

}
