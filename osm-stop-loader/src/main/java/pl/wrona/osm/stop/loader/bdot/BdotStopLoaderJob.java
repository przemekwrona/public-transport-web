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
import org.springframework.util.ResourceUtils;
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
        String filePath = "classpath:1005_SHP";

        File pbfFile = ResourceUtils.getFile(filePath);

        Files.walk(pbfFile.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".shp"))
                .filter(path -> path.toString().endsWith("OT_OIKM_P.shp"))
                .limit(1)
                .forEach((Path path) -> {
                    log.info("Processing file {}", path);
                    this.processShp(path);
                });
    }

    public void processShp(Path shpPath) {
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

                    if (feature.getDefaultGeometry() instanceof Point) {
                        String stopId = (String) feature.getAttribute("LOKALNYID");
                        String stopName = (String) feature.getAttribute("NAZWA");
                        Point bdok10kPoint = (Point) feature.getDefaultGeometry();

                        Point wgs84Point = bdot10kCoordinateProjection.apply(bdok10kPoint);

                        stopResource.save(stopId, stopName, wgs84Point.getX(), wgs84Point.getY());
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
