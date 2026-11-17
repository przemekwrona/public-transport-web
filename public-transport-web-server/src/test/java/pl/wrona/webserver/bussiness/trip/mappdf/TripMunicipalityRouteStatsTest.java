package pl.wrona.webserver.bussiness.trip.mappdf;

import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Subdivision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TripMunicipalityRouteStatsTest {

    @Test
    void percentagesSumToOneHundredWithTwoDecimals() {
        List<Point2D> route = List.of(
                point(50.10, 20.00),
                point(50.20, 20.00),
                point(50.30, 20.00)
        );
        List<Subdivision> subdivisions = List.of(
                subdivision("1", "Alpha", "POLYGON ((19.9 50.05, 20.1 50.05, 20.1 50.20, 19.9 50.20, 19.9 50.05))"),
                subdivision("2", "Beta", "POLYGON ((19.9 50.20, 20.1 50.20, 20.1 50.35, 19.9 50.35, 19.9 50.20))")
        );

        List<TripMapPdfView.Municipality> stats = TripMunicipalityRouteStats.withDistanceShares(route, subdivisions);

        assertThat(stats).hasSize(2);
        BigDecimal sum = stats.stream()
                .map(item -> new BigDecimal(item.percent()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualByComparingTo("100.00");
        assertThat(stats).allSatisfy(item -> {
            assertThat(item.kilometers()).matches("\\d+\\.\\d{2}");
            assertThat(item.percent()).matches("\\d+\\.\\d{2}");
            assertThat(item.gmina()).startsWith("gmina ");
        });
    }

    @Test
    void largestRemainderKeepsExactHundred() {
        List<TripMunicipalityRouteStats.ShareDraft> drafts = new ArrayList<>(List.of(
                new TripMunicipalityRouteStats.ShareDraft(null, 1, 33.333),
                new TripMunicipalityRouteStats.ShareDraft(null, 1, 33.333),
                new TripMunicipalityRouteStats.ShareDraft(null, 1, 33.334)
        ));

        TripMunicipalityRouteStats.applyLargestRemainderPercents(drafts);

        double sum = drafts.stream().mapToDouble(TripMunicipalityRouteStats.ShareDraft::roundedPercent).sum();
        assertThat(sum).isCloseTo(100.0, within(0.0001));
        assertThat(drafts).allSatisfy(draft ->
                assertThat(BigDecimal.valueOf(draft.roundedPercent()).scale()).isLessThanOrEqualTo(2));
    }

    private static Point2D point(double lat, double lon) {
        return new Point2D().lat((float) lat).lon((float) lon);
    }

    private static Subdivision subdivision(String teryt, String gmina, String geometry) {
        return new Subdivision()
                .teryt(teryt)
                .wojewodztwo("WOJ")
                .powiat("POW")
                .gmina(gmina)
                .geometry(geometry);
    }
}
