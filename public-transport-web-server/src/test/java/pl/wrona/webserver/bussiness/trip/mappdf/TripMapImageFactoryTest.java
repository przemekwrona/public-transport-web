package pl.wrona.webserver.bussiness.trip.mappdf;

import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Subdivision;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TripMapImageFactoryTest {

    @Test
    void buildsPngDataUriWithRouteAndMunicipality() throws Exception {
        byte[] png = TripMapImageFactory.renderPng(
                List.of(
                        new Point2D().lat(50.1f).lon(19.9f),
                        new Point2D().lat(50.2f).lon(20.0f)
                ),
                List.of(new Subdivision()
                        .teryt("1261011")
                        .geometry("POLYGON ((19.8 50.0, 20.1 50.0, 20.1 50.3, 19.8 50.3, 19.8 50.0))"))
        );

        assertThat(png).isNotEmpty();
        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(png[1]).isEqualTo((byte) 0x50);

        String html = TripMapImageFactory.buildHtmlImage(
                List.of(
                        new Point2D().lat(50.1f).lon(19.9f),
                        new Point2D().lat(50.2f).lon(20.0f)
                ),
                List.of()
        );
        assertThat(html).startsWith("<img src=\"data:image/png;base64,");
    }

    @Test
    void formatsMunicipalityLabelWithGminaPrefix() {
        assertThat(TripMapImageFactory.municipalityLabel("Chmielnik")).isEqualTo("gmina Chmielnik");
        assertThat(TripMapImageFactory.municipalityLabel("gmina Kielce")).isEqualTo("gmina Kielce");
    }

    @Test
    void preservesAspectRatioForEqualDegreeSquareNearPoland() {
        TripMapImageFactory.Viewport viewport = TripMapImageFactory.Viewport.from(20.0, 21.0, 50.0, 51.0);

        double widthPx = viewport.x(21.0) - viewport.x(20.0);
        double heightPx = viewport.y(50.0) - viewport.y(51.0);
        double expectedRatio = Math.cos(Math.toRadians(50.5));

        assertThat(widthPx / heightPx).isCloseTo(expectedRatio, within(0.001));
    }

    @Test
    void invertsPixelCoordinatesOutsideDataBounds() {
        // Wide canvas vs tall geography → horizontal letterbox margins.
        TripMapImageFactory.Viewport viewport = TripMapImageFactory.Viewport.from(20.4, 20.5, 50.0, 51.0);

        double lonLeftOfData = viewport.lon(viewport.x(20.4) - 30);
        double lonRightOfData = viewport.lon(viewport.x(20.5) + 30);

        assertThat(lonLeftOfData).isLessThan(20.4);
        assertThat(lonRightOfData).isGreaterThan(20.5);
        assertThat(viewport.x(lonLeftOfData)).isCloseTo(viewport.x(20.4) - 30, within(0.01));
    }
}
