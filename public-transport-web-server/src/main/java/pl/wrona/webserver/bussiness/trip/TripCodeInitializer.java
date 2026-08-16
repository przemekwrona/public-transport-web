package pl.wrona.webserver.bussiness.trip;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.route.RouteQueryRepository;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;
import pl.wrona.webserver.core.agency.TripEntity;

@Slf4j
@Order(2)
@Service
@AllArgsConstructor
public class TripCodeInitializer implements ApplicationRunner {

    private final AgencyRepository agencyRepository;
    private final RouteQueryRepository routeQueryRepository;
    private final TripQueryRepository tripQueryRepository;
    private final TripCommandRepository tripCommandRepository;
    private final TripSequenceCommandService tripSequenceCommandService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (AgencyEntity agency : agencyRepository.findAll()) {
            updateTripsForAgency(agency);
        }
    }

    private void updateTripsForAgency(AgencyEntity agency) {
        var routes = routeQueryRepository.findByAgencyCode(agency.getAgencyCode());
        int totalTrips = 0;

        for (RouteEntity route : routes) {
            totalTrips += updateTripsForRoute(agency.getAgencyCode(), route);
        }

        log.info("Initialized trip codes for agency {} ({} trips across {} routes)",
                agency.getAgencyCode(), totalTrips, routes.size());
    }

    private int updateTripsForRoute(String agencyCode, RouteEntity route) {
        if (route.getRouteSequence() == null) {
            return 0;
        }

        var trips = tripQueryRepository.findByRouteIdOrderByCreatedAtAsc(route.getRouteId());

        int sequence = 1;
        for (TripEntity trip : trips) {
            trip.setTripSequence(sequence);
            trip.setTripCode(Hex.toHex3(sequence));
            sequence++;
        }

        tripCommandRepository.saveAll(trips);
        tripSequenceCommandService.saveNextValue(agencyCode, route.getRouteSequence(), sequence);

        return trips.size();
    }

}
