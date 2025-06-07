package pl.wrona.osm.stop.deactivate;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.osm.stop.deactivate.stop.StopService;

@Service
@AllArgsConstructor
public class OsmDeactivateService {

    private final StopService stopService;
}
