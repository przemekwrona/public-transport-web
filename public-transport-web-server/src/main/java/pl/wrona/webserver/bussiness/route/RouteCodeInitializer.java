package pl.wrona.webserver.bussiness.route;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.core.AgencyRepository;
import pl.wrona.webserver.core.agency.AgencyEntity;
import pl.wrona.webserver.core.agency.RouteEntity;

@Slf4j
@Order(1)
@Service
@AllArgsConstructor
public class RouteCodeInitializer implements ApplicationRunner {

    private final AgencyRepository agencyRepository;
    private final RouteQueryRepository routeQueryRepository;
    private final RouteCommandRepository routeCommandRepository;
    private final RouteSequenceCommandService routeSequenceCommandService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (AgencyEntity agency : agencyRepository.findAll()) {
            updateRoutesForAgency(agency);
        }
    }

    private void updateRoutesForAgency(AgencyEntity agency) {
        var routes = routeQueryRepository.findByAgencyCodeOrderByCreatedAtAsc(agency.getAgencyCode());

        int sequence = 1;
        for (RouteEntity route : routes) {
            route.setRouteSequence(sequence);
            route.setRouteCode(Hex.toHex5(sequence));
            sequence++;
        }

        routeCommandRepository.saveAll(routes);
        routeSequenceCommandService.saveNextValue(agency.getAgencyCode(), sequence);

        log.info("Initialized route codes for agency {} ({} routes)", agency.getAgencyCode(), routes.size());
    }

}
