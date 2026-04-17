package pl.wrona.webserver.bussiness.gtfs.download;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.brigade.BrigadeTripEntity;
import pl.wrona.webserver.core.calendar.CalendarSymbolEntity;

import java.util.List;

@Service
@AllArgsConstructor
public class GtfsBrigadeTripService {

    private final GtfsBrigadeTripRepository gtfsBrigadeTripRepository;


    public List<BrigadeTripEntity> findAllByAgencyAndActiveCalendars(AgencyEntity agency, List<CalendarSymbolEntity> calendars) {
        return gtfsBrigadeTripRepository.findAllByAgencyAndActiveCalendars(agency, calendars);
    }
}
