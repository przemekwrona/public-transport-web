package pl.wrona.osm.stop.deactivate;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OsmDeactivateCommandLine implements CommandLineRunner {

    private final OsmDeactivateService osmDeactivateService;

    @Override
    public void run(String... args) throws Exception {

    }
}
