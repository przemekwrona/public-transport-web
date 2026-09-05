package pl.wrona.webserver.bussiness.brigade.group;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupQueryRepository;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeGroupQueryService {

    private final BrigadeGroupQueryRepository brigadeGroupQueryRepository;

    public BrigadeGroupEntity findByBrigadeCode(String instance, String brigadeCode, String calendarCode, String calendarSymbol) {
        return brigadeGroupQueryRepository.findBtBrigadeCode(instance, brigadeCode, calendarCode, calendarSymbol);
    }

    public List<BrigadeGroupEntity> findAll(String instance) {
        return brigadeGroupQueryRepository.findAllByAgencyCode(instance);
    }

    public List<BrigadeGroupEntity> findAllByBrigadeCode(String instance, String brigadeCode) {
        return brigadeGroupQueryRepository.findAllByAgencyCodeAndBrigadeCode(instance, brigadeCode);
    }

    public boolean existsByCalendarSymbol(CalendarSymbolEntity calendarSymbol) {
        return brigadeGroupQueryRepository.existsByCalendarSymbol(calendarSymbol);
    }

}
