package pl.wrona.osm.stop.loader.bdot;

import lombok.AllArgsConstructor;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.TransformException;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@AllArgsConstructor
public class Bdot10kCoordinateProjection implements Function<Point, Point> {

    private final MathTransform bdot10kTransformation;

    @Override
    public Point apply(Point point) {

        Point projectedPoint = null;
        try {
            projectedPoint = (Point) org.geotools.geometry.jts.JTS.transform(point, bdot10kTransformation);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        }

        return projectedPoint;
    }

}
