package pl.wrona.webserver.bussiness.trip.mappdf;

import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Subdivision;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class TripMunicipalityRouteStats {

    private static final double EARTH_RADIUS_M = 6_371_008.8;
    private static final double SAMPLE_STEP_M = 25.0;

    private TripMunicipalityRouteStats() {
    }

    static List<TripMapPdfView.Municipality> withDistanceShares(
            List<Point2D> routePoints,
            List<Subdivision> subdivisions) {
        if (subdivisions == null || subdivisions.isEmpty()) {
            return List.of();
        }

        List<MunicipalityGeometry> geometries = subdivisions.stream()
                .filter(item -> item != null && item.getTeryt() != null)
                .map(item -> new MunicipalityGeometry(
                        item.getTeryt(),
                        item.getWojewodztwo(),
                        item.getPowiat(),
                        TripMapImageFactory.municipalityLabel(item.getGmina()),
                        parseRings(item.getGeometry())))
                .filter(item -> !item.rings().isEmpty())
                .toList();
        if (geometries.isEmpty()) {
            return subdivisions.stream()
                    .map(item -> new TripMapPdfView.Municipality(
                            item.getTeryt(),
                            item.getWojewodztwo(),
                            item.getPowiat(),
                            TripMapImageFactory.municipalityLabel(item.getGmina()),
                            "0.00",
                            "0.00"))
                    .toList();
        }

        List<double[]> route = densify(toLonLat(routePoints), SAMPLE_STEP_M);
        Map<String, Double> metersByTeryt = new HashMap<>();
        geometries.forEach(geometry -> metersByTeryt.put(geometry.teryt(), 0.0));

        String lastTeryt = geometries.get(0).teryt();
        for (int i = 1; i < route.size(); i++) {
            double[] from = route.get(i - 1);
            double[] to = route.get(i);
            double segmentMeters = haversineMeters(from[1], from[0], to[1], to[0]);
            if (segmentMeters <= 0) {
                continue;
            }
            double midLon = (from[0] + to[0]) / 2.0;
            double midLat = (from[1] + to[1]) / 2.0;
            String teryt = containingTeryt(geometries, midLon, midLat);
            if (teryt == null) {
                teryt = lastTeryt;
            } else {
                lastTeryt = teryt;
            }
            metersByTeryt.merge(teryt, segmentMeters, Double::sum);
        }

        double totalMeters = metersByTeryt.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalMeters <= 0) {
            return geometries.stream()
                    .map(item -> new TripMapPdfView.Municipality(
                            item.teryt(),
                            item.wojewodztwo(),
                            item.powiat(),
                            item.gmina(),
                            "0.00",
                            "0.00"))
                    .toList();
        }

        List<ShareDraft> drafts = new ArrayList<>();
        for (MunicipalityGeometry geometry : geometries) {
            double meters = metersByTeryt.getOrDefault(geometry.teryt(), 0.0);
            double rawPercent = meters * 100.0 / totalMeters;
            drafts.add(new ShareDraft(geometry, meters, rawPercent));
        }

        applyLargestRemainderPercents(drafts);

        return drafts.stream()
                .sorted(Comparator.comparing(ShareDraft::rawPercent).reversed()
                        .thenComparing(draft -> draft.geometry().teryt()))
                .map(draft -> new TripMapPdfView.Municipality(
                        draft.geometry().teryt(),
                        draft.geometry().wojewodztwo(),
                        draft.geometry().powiat(),
                        draft.geometry().gmina(),
                        formatKm(draft.meters() / 1000.0),
                        formatPercent(draft.roundedPercent())))
                .toList();
    }

    static void applyLargestRemainderPercents(List<ShareDraft> drafts) {
        int totalCents = 10_000;
        int[] cents = new int[drafts.size()];
        List<Remainder> remainders = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < drafts.size(); i++) {
            ShareDraft draft = drafts.get(i);
            BigDecimal rawCents = BigDecimal.valueOf(draft.rawPercent()).movePointRight(2);
            int floorCents = rawCents.setScale(0, RoundingMode.DOWN).intValue();
            BigDecimal fraction = rawCents.subtract(BigDecimal.valueOf(floorCents));
            cents[i] = floorCents;
            assigned += floorCents;
            remainders.add(new Remainder(i, fraction));
        }

        int missing = totalCents - assigned;
        remainders.sort(Comparator.comparing(Remainder::fraction).reversed()
                .thenComparingInt(Remainder::index));
        for (int i = 0; i < missing && i < remainders.size(); i++) {
            cents[remainders.get(i).index()]++;
        }
        for (int i = 0; i < drafts.size(); i++) {
            drafts.set(i, drafts.get(i).withRoundedPercent(cents[i] / 100.0));
        }
    }

    private static String containingTeryt(List<MunicipalityGeometry> geometries, double lon, double lat) {
        for (MunicipalityGeometry geometry : geometries) {
            if (contains(geometry.rings(), lon, lat)) {
                return geometry.teryt();
            }
        }
        return null;
    }

    private static boolean contains(List<List<double[]>> rings, double lon, double lat) {
        for (List<double[]> ring : rings) {
            if (pointInRing(ring, lon, lat)) {
                return true;
            }
        }
        return false;
    }

    private static boolean pointInRing(List<double[]> ring, double lon, double lat) {
        boolean inside = false;
        for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
            double xi = ring.get(i)[0];
            double yi = ring.get(i)[1];
            double xj = ring.get(j)[0];
            double yj = ring.get(j)[1];
            boolean intersect = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi + 0.0) + xi);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static List<double[]> densify(List<double[]> route, double stepMeters) {
        if (route.size() < 2) {
            return route;
        }
        List<double[]> densified = new ArrayList<>();
        densified.add(route.get(0));
        for (int i = 1; i < route.size(); i++) {
            double[] from = route.get(i - 1);
            double[] to = route.get(i);
            double distance = haversineMeters(from[1], from[0], to[1], to[0]);
            if (distance <= stepMeters) {
                densified.add(to);
                continue;
            }
            int steps = (int) Math.ceil(distance / stepMeters);
            for (int step = 1; step <= steps; step++) {
                double ratio = (double) step / steps;
                densified.add(new double[]{
                        from[0] + (to[0] - from[0]) * ratio,
                        from[1] + (to[1] - from[1]) * ratio
                });
            }
        }
        return densified;
    }

    private static List<double[]> toLonLat(List<Point2D> routePoints) {
        if (routePoints == null) {
            return List.of();
        }
        List<double[]> points = new ArrayList<>();
        for (Point2D point : routePoints) {
            if (point == null || point.getLat() == null || point.getLon() == null) {
                continue;
            }
            points.add(new double[]{point.getLon(), point.getLat()});
        }
        return points;
    }

    private static List<List<double[]>> parseRings(String wkt) {
        List<List<double[]>> rings = new ArrayList<>();
        if (wkt == null || wkt.isBlank()) {
            return rings;
        }
        String trimmed = wkt.trim();
        int start = trimmed.toUpperCase(Locale.ROOT).indexOf("((");
        if (start < 0) {
            return rings;
        }
        String body = trimmed.substring(start);
        String[] parts = body.split("\\)\\s*,\\s*\\(");
        for (String part : parts) {
            String cleaned = part.replace("(", "").replace(")", "").trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            List<double[]> ring = new ArrayList<>();
            for (String pair : cleaned.split(",")) {
                String[] xy = pair.trim().split("\\s+");
                if (xy.length < 2) {
                    continue;
                }
                try {
                    ring.add(new double[]{Double.parseDouble(xy[0]), Double.parseDouble(xy[1])});
                } catch (NumberFormatException ignored) {
                    // skip invalid coordinates
                }
            }
            if (ring.size() >= 3) {
                rings.add(ring);
            }
        }
        return rings;
    }

    private static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    private static String formatKm(double kilometers) {
        return BigDecimal.valueOf(kilometers).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String formatPercent(double percent) {
        return BigDecimal.valueOf(percent).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private record MunicipalityGeometry(
            String teryt,
            String wojewodztwo,
            String powiat,
            String gmina,
            List<List<double[]>> rings
    ) {
    }

    record ShareDraft(MunicipalityGeometry geometry, double meters, double rawPercent, double roundedPercent) {
        ShareDraft(MunicipalityGeometry geometry, double meters, double rawPercent) {
            this(geometry, meters, rawPercent, 0.0);
        }

        ShareDraft withRoundedPercent(double roundedPercent) {
            return new ShareDraft(geometry, meters, rawPercent, roundedPercent);
        }
    }

    private record Remainder(int index, BigDecimal fraction) {
    }
}
