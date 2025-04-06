package pl.wrona.osm.stop.loader.osm;


import crosby.binary.osmosis.OsmosisReader;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;

@Slf4j
@Component
@AllArgsConstructor
public class OsmStopLoaderJob implements CommandLineRunner {

    private final BusStopSink busStopSink;

    @Override
    public void run(String... args) throws Exception {
        String filePath = "classpath:poland-latest.osm.pbf";
        File pbfFile = ResourceUtils.getFile(filePath);

        // Create the PBF Reader with multithreading support
        OsmosisReader reader = new OsmosisReader(pbfFile);

        // Set the sink (handler for entities)
        reader.setSink(busStopSink);

        // Start reading
//        reader.run();
    }
}
