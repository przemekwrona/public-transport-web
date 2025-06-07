package pl.wrona.osm.stop.deactivate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.osm.stop.deactivate.stop.StopEntity;
import pl.wrona.osm.stop.deactivate.stop.StopRepository;
import pl.wrona.osm.stop.deactivate.stop.StopService;

@Slf4j
@Service
@AllArgsConstructor
public class OsmDeactivateService {

    private final int MAX_PAGES = 2;

    private final StopService stopService;
    private final StopRepository stopRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public void deactivateOsmStops() {
        for (int page = 0; page < MAX_PAGES; page++) {
            deactivateOsmStops(page);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deactivateOsmStops(int page) {
        var stops = stopService.findNext100NotCheckedStops(page);

        stops.forEach(stop -> {
            stop.setDeactivatedChecked(true);

            if (stopService.hasBdot10kStopIn10m(stop)) {
                stop.setActive(false);
            }
        });

        stopRepository.saveAll(stops);
        log.info("Page {}/{} DONE. Checking next 1000 stops", page + 1, MAX_PAGES);
    }
}
