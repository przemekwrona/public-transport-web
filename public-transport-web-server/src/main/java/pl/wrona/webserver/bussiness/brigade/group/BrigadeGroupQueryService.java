package pl.wrona.webserver.bussiness.brigade.group;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.brigade.BrigadeGroupEntity;
import pl.wrona.webserver.core.brigade.BrigadeGroupQueryRepository;

@Service
@AllArgsConstructor
public class BrigadeGroupQueryService {

    private final BrigadeGroupQueryRepository brigadeGroupQueryRepository;

    public BrigadeGroupEntity findByCalendarCodeAndCalendarSymbol(String instance, String calendarCode, String calendarSymbol) {
        return brigadeGroupQueryRepository.findByCalendarSymbol(instance, calendarCode, calendarSymbol);
    }

}
