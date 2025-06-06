package pl.wrona.webserver.agency.brigade;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.agency.AgencyService;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class BrigadeTripService {

    private final AgencyService agencyService;
    private final BrigadeTripRepository brigadeTripRepository;

    public List<BrigadeTripEntity> findAllBrigadeTripsForActiveCalendar() {
        return brigadeTripRepository.findAllByAgencyAndActiveCalendar(agencyService.getLoggedAgency(), LocalDate.now());
    }
}
