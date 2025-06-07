package pl.wrona.osm.stop.deactivate.stop;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StopService {

    private final StopRepository stopRepository;
}
