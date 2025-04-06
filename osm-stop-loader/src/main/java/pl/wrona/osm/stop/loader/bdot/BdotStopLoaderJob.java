package pl.wrona.osm.stop.loader.bdot;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Point;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pl.wrona.osm.stop.loader.StopResource;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class BdotStopLoaderJob implements CommandLineRunner {

    private final Bdot10kCoordinateProjection bdot10kCoordinateProjection;
    private final StopResource stopResource;

    @Override
    public void run(String... args) throws Exception {
        String filePath = "/Users/wronap/workspace/public-transport-web/Polska_SHP";

        Files.walk(Path.of(filePath))
                .filter(path -> path.toString().endsWith(".shp"))
                .filter(path -> path.toString().endsWith("OT_OIKM_P.shp"))
                .forEach(this::processShp);
    }

    public void processShp(Path shpPath) {
        log.info("Processing shp file: {}", shpPath);
        try {
            File file = shpPath.toFile();
            Map<String, String> connect = Map.of("url", file.toURI().toString());


            DataStore dataStore = DataStoreFinder.getDataStore(connect);
            String[] typeNames = dataStore.getTypeNames();
            String typeName = typeNames[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures();

            try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
                while (iterator.hasNext()) {
                    SimpleFeature feature = iterator.next();

                    if (feature.getDefaultGeometry() instanceof Point bdok10kPoint) {
                        String stopId = (String) feature.getAttribute("LOKALNYID");
                        String stopName = (String) feature.getAttribute("NAZWA");
                        String formattedStopName = stopName.replaceAll("'", "''");

                        Point wgs84Point = bdot10kCoordinateProjection.apply(bdok10kPoint);

                        stopResource.save(stopId, formattedStopName, wgs84Point.getY(), wgs84Point.getX());
                    }
                }
            }

            dataStore.dispose();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
