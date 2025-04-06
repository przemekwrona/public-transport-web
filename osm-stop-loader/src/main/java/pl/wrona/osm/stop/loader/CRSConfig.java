package pl.wrona.osm.stop.loader;

import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.referencing.CRS;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration()
public class CRSConfig {

    @Bean
    public CoordinateReferenceSystem bdot10kCRS() throws FactoryException {
        return CRS.decode("EPSG:2180");
    }

    @Bean
    public CoordinateReferenceSystem wgs84CRS() throws FactoryException {
        return CRS.decode("EPSG:4326");
    }

    @Bean
    public MathTransform bdot10kTransformation(CoordinateReferenceSystem bdot10kCRS, CoordinateReferenceSystem wgs84CRS) throws FactoryException {
        return CRS.findMathTransform(bdot10kCRS, wgs84CRS, true);
    }

}
