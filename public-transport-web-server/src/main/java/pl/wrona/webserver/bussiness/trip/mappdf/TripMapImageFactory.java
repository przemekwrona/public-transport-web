package pl.wrona.webserver.bussiness.trip.mappdf;

import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Subdivision;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class TripMapImageFactory {

    private static final int WIDTH = 720;
    private static final int HEIGHT = 520;
    /** Left margin reserved for latitude labels (e.g. {@code 50°42'N}). */
    private static final int PAD_LEFT = 58;
    private static final int PAD_RIGHT = 24;
    private static final int PAD_TOP = 24;
    /** Bottom margin reserved for longitude labels. */
    private static final int PAD_BOTTOM = 28;
    private static final Color BACKGROUND = new Color(0xff, 0xff, 0xff);
    private static final Color BORDER = new Color(0xcb, 0xd5, 0xe1);
    private static final Color MUNICIPALITY_STROKE = new Color(0x25, 0x63, 0xeb);
    private static final Color ROUTE_STROKE = new Color(0xdc, 0x26, 0x26);
    private static final Color START_COLOR = new Color(0x16, 0xa3, 0x4a);
    private static final Color END_COLOR = new Color(0x7c, 0x3a, 0xed);
    private static final Color EMPTY_TEXT = new Color(0x64, 0x74, 0x8b);
    private static final Color LABEL_BG = new Color(255, 255, 255, 210);
    private static final Color LABEL_TEXT = new Color(0x1e, 0x3a, 0x8a);
    private static final Color GRID_MINUTE = new Color(0xe2, 0xe8, 0xf0);
    private static final Color GRID_DEGREE = new Color(0x94, 0xa3, 0xb8);
    private static final Color GRID_LABEL = new Color(0x47, 0x55, 0x69);
    private static final double MINUTE_DEG = 1.0 / 60.0;

    private TripMapImageFactory() {
    }

    static String buildHtmlImage(List<Point2D> routePoints, List<Subdivision> municipalities) {
        try {
            byte[] png = renderPng(routePoints, municipalities);
            String base64 = Base64.getEncoder().encodeToString(png);
            return "<img src=\"data:image/png;base64," + base64 + "\" width=\"" + WIDTH
                    + "\" height=\"" + HEIGHT + "\" alt=\"Mapa trasy\" />";
        } catch (IOException exception) {
            return "<div>Nie udało się narysować mapy trasy</div>";
        }
    }

    static byte[] renderPng(List<Point2D> routePoints, List<Subdivision> municipalities) throws IOException {
        List<double[]> route = toLonLat(routePoints);
        List<LabeledRing> labeledRings = labeledRings(municipalities);

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setColor(BACKGROUND);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            graphics.setColor(BORDER);
            graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

            List<List<double[]>> rings = labeledRings.stream().map(LabeledRing::ring).toList();
            Viewport viewport = viewport(route, rings);
            if (viewport == null) {
                graphics.setColor(EMPTY_TEXT);
                graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
                String message = "Brak geometrii trasy";
                int textWidth = graphics.getFontMetrics().stringWidth(message);
                graphics.drawString(message, (WIDTH - textWidth) / 2, HEIGHT / 2);
            } else {
                drawCoordinateGrid(graphics, viewport);

                for (LabeledRing labeledRing : labeledRings) {
                    Path2D path = toPath(labeledRing.ring(), viewport, true);
                    graphics.setColor(MUNICIPALITY_STROKE);
                    graphics.setStroke(new BasicStroke(1.2f));
                    graphics.draw(path);
                }

                if (route.size() >= 2) {
                    Path2D routePath = toPath(route, viewport, false);
                    graphics.setColor(ROUTE_STROKE);
                    graphics.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    graphics.draw(routePath);

                    double[] start = route.get(0);
                    double[] end = route.get(route.size() - 1);
                    drawPoint(graphics, viewport.x(start[0]), viewport.y(start[1]), START_COLOR);
                    drawPoint(graphics, viewport.x(end[0]), viewport.y(end[1]), END_COLOR);
                }

                graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
                for (LabeledRing labeledRing : labeledRings) {
                    if (labeledRing.label() == null || labeledRing.label().isBlank()) {
                        continue;
                    }
                    double[] centroid = centroid(labeledRing.ring());
                    drawLabel(
                            graphics,
                            municipalityLabel(labeledRing.label()),
                            viewport.x(centroid[0]),
                            viewport.y(centroid[1]));
                }
            }
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", output);
            return output.toByteArray();
        }
    }

    private static List<LabeledRing> labeledRings(List<Subdivision> municipalities) {
        if (municipalities == null) {
            return List.of();
        }
        List<LabeledRing> labeledRings = new ArrayList<>();
        for (Subdivision municipality : municipalities) {
            if (municipality == null || municipality.getGeometry() == null || municipality.getGeometry().isBlank()) {
                continue;
            }
            List<List<double[]>> rings = parseRings(municipality.getGeometry()).stream()
                    .filter(ring -> ring.size() >= 3)
                    .toList();
            if (rings.isEmpty()) {
                continue;
            }
            List<double[]> mainRing = rings.stream()
                    .max(Comparator.comparingInt(List::size))
                    .orElse(rings.get(0));
            labeledRings.add(new LabeledRing(municipality.getGmina(), mainRing));
            for (List<double[]> ring : rings) {
                if (ring != mainRing) {
                    labeledRings.add(new LabeledRing(null, ring));
                }
            }
        }
        return labeledRings;
    }

    static String municipalityLabel(String gminaName) {
        if (gminaName == null || gminaName.isBlank()) {
            return "";
        }
        String trimmed = gminaName.trim();
        if (trimmed.toLowerCase(Locale.ROOT).startsWith("gmina ")) {
            return trimmed;
        }
        return "gmina " + trimmed;
    }

    private static void drawLabel(Graphics2D graphics, String label, double x, double y) {
        FontMetrics metrics = graphics.getFontMetrics();
        int textWidth = metrics.stringWidth(label);
        int textHeight = metrics.getAscent();
        int paddingX = 6;
        int paddingY = 4;
        int boxX = (int) Math.round(x - (textWidth / 2.0) - paddingX);
        int boxY = (int) Math.round(y - (textHeight / 2.0) - paddingY);
        int boxW = textWidth + paddingX * 2;
        int boxH = textHeight + paddingY * 2;

        graphics.setColor(LABEL_BG);
        graphics.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        graphics.setColor(MUNICIPALITY_STROKE);
        graphics.setStroke(new BasicStroke(1f));
        graphics.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        graphics.setColor(LABEL_TEXT);
        graphics.drawString(label, boxX + paddingX, boxY + paddingY + textHeight - 2);
    }

    private static double[] centroid(List<double[]> ring) {
        double sumLon = 0;
        double sumLat = 0;
        for (double[] point : ring) {
            sumLon += point[0];
            sumLat += point[1];
        }
        return new double[]{sumLon / ring.size(), sumLat / ring.size()};
    }

    private static void drawPoint(Graphics2D graphics, double x, double y, Color color) {
        graphics.setColor(color);
        graphics.fill(new Ellipse2D.Double(x - 5, y - 5, 10, 10));
    }

    private static void drawCoordinateGrid(Graphics2D graphics, Viewport viewport) {
        // Extend grid into letterbox margins so empty sides still get minute lines.
        double gridMinLon = viewport.lon(PAD_LEFT);
        double gridMaxLon = viewport.lon(WIDTH - PAD_RIGHT);
        double gridMinLat = viewport.lat(HEIGHT - PAD_BOTTOM);
        double gridMaxLat = viewport.lat(PAD_TOP);

        double left = viewport.x(gridMinLon);
        double right = viewport.x(gridMaxLon);
        double top = viewport.y(gridMaxLat);
        double bottom = viewport.y(gridMinLat);

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));

        double firstLon = Math.ceil(gridMinLon / MINUTE_DEG) * MINUTE_DEG;
        for (double lon = firstLon; lon <= gridMaxLon + 1e-12; lon += MINUTE_DEG) {
            boolean degreeLine = isWholeDegree(lon);
            double x = viewport.x(lon);
            graphics.setColor(degreeLine ? GRID_DEGREE : GRID_MINUTE);
            graphics.setStroke(new BasicStroke(degreeLine ? 1.2f : 0.7f));
            graphics.drawLine((int) Math.round(x), (int) Math.round(top), (int) Math.round(x), (int) Math.round(bottom));
            if (degreeLine || isFiveMinute(lon)) {
                drawGridLabel(graphics, formatLon(lon), x, bottom + 12, true);
            }
        }

        double firstLat = Math.ceil(gridMinLat / MINUTE_DEG) * MINUTE_DEG;
        for (double lat = firstLat; lat <= gridMaxLat + 1e-12; lat += MINUTE_DEG) {
            boolean degreeLine = isWholeDegree(lat);
            double y = viewport.y(lat);
            graphics.setColor(degreeLine ? GRID_DEGREE : GRID_MINUTE);
            graphics.setStroke(new BasicStroke(degreeLine ? 1.2f : 0.7f));
            graphics.drawLine((int) Math.round(left), (int) Math.round(y), (int) Math.round(right), (int) Math.round(y));
            if (degreeLine || isFiveMinute(lat)) {
                // Right-align into the left margin so labels are fully visible.
                drawGridLabel(graphics, formatLat(lat), left - 6, y + 3, false);
            }
        }
    }

    private static void drawGridLabel(Graphics2D graphics, String label, double x, double y, boolean centered) {
        graphics.setColor(GRID_LABEL);
        FontMetrics metrics = graphics.getFontMetrics();
        int textX = centered
                ? (int) Math.round(x - metrics.stringWidth(label) / 2.0)
                : (int) Math.round(x - metrics.stringWidth(label));
        // Keep labels inside the image (left/right edges).
        textX = Math.max(2, Math.min(textX, WIDTH - metrics.stringWidth(label) - 2));
        graphics.drawString(label, textX, (int) Math.round(y));
    }

    private static boolean isWholeDegree(double value) {
        double minutes = Math.abs(value) * 60.0;
        return Math.abs(minutes - Math.rint(minutes / 60.0) * 60.0) < 1e-6;
    }

    private static boolean isFiveMinute(double value) {
        double minutes = Math.abs(value) * 60.0;
        double rounded = Math.rint(minutes);
        return Math.abs(minutes - rounded) < 1e-6 && ((int) Math.round(rounded) % 5) == 0;
    }

    private static String formatLon(double lon) {
        return formatDms(lon, "E", "W");
    }

    private static String formatLat(double lat) {
        return formatDms(lat, "N", "S");
    }

    private static String formatDms(double value, String positiveHemisphere, String negativeHemisphere) {
        String hemisphere = value >= 0 ? positiveHemisphere : negativeHemisphere;
        double absolute = Math.abs(value);
        int degrees = (int) Math.floor(absolute + 1e-9);
        int minutes = (int) Math.floor((absolute - degrees) * 60.0 + 1e-9);
        if (minutes >= 60) {
            degrees += 1;
            minutes = 0;
        }
        return String.format(Locale.ROOT, "%d°%02d'%s", degrees, minutes, hemisphere);
    }

    private static Path2D toPath(List<double[]> points, Viewport viewport, boolean closed) {
        Path2D path = new Path2D.Double();
        for (int i = 0; i < points.size(); i++) {
            double x = viewport.x(points.get(i)[0]);
            double y = viewport.y(points.get(i)[1]);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        if (closed) {
            path.closePath();
        }
        return path;
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

    private static Viewport viewport(List<double[]> route, List<List<double[]>> rings) {
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        boolean found = false;

        for (double[] point : route) {
            minLon = Math.min(minLon, point[0]);
            maxLon = Math.max(maxLon, point[0]);
            minLat = Math.min(minLat, point[1]);
            maxLat = Math.max(maxLat, point[1]);
            found = true;
        }
        for (List<double[]> ring : rings) {
            for (double[] point : ring) {
                minLon = Math.min(minLon, point[0]);
                maxLon = Math.max(maxLon, point[0]);
                minLat = Math.min(minLat, point[1]);
                maxLat = Math.max(maxLat, point[1]);
                found = true;
            }
        }
        if (!found) {
            return null;
        }
        if (minLon == maxLon) {
            minLon -= 0.01;
            maxLon += 0.01;
        }
        if (minLat == maxLat) {
            minLat -= 0.01;
            maxLat += 0.01;
        }
        return Viewport.from(minLon, maxLon, minLat, maxLat);
    }

    /**
     * Equirectangular projection with longitude scaled by {@code cos(midLat)} and fitted
     * into the canvas while preserving aspect ratio (WGS84 lon/lat stay visually flat).
     */
    static final class Viewport {
        private final double minLon;
        private final double maxLon;
        private final double minLat;
        private final double maxLat;
        private final double lonScale;
        private final double originX;
        private final double originY;
        private final double scale;

        private Viewport(
                double minLon,
                double maxLon,
                double minLat,
                double maxLat,
                double lonScale,
                double originX,
                double originY,
                double scale) {
            this.minLon = minLon;
            this.maxLon = maxLon;
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.lonScale = lonScale;
            this.originX = originX;
            this.originY = originY;
            this.scale = scale;
        }

        static Viewport from(double minLon, double maxLon, double minLat, double maxLat) {
            double midLat = (minLat + maxLat) / 2.0;
            double lonScale = Math.max(0.2, Math.cos(Math.toRadians(midLat)));
            double widthUnits = (maxLon - minLon) * lonScale;
            double heightUnits = maxLat - minLat;
            double usableWidth = WIDTH - PAD_LEFT - PAD_RIGHT;
            double usableHeight = HEIGHT - PAD_TOP - PAD_BOTTOM;
            double scale = Math.min(usableWidth / widthUnits, usableHeight / heightUnits);
            double drawnWidth = widthUnits * scale;
            double drawnHeight = heightUnits * scale;
            double originX = PAD_LEFT + (usableWidth - drawnWidth) / 2.0;
            double originY = PAD_TOP + (usableHeight - drawnHeight) / 2.0;
            return new Viewport(minLon, maxLon, minLat, maxLat, lonScale, originX, originY, scale);
        }

        double minLon() {
            return minLon;
        }

        double maxLon() {
            return maxLon;
        }

        double minLat() {
            return minLat;
        }

        double maxLat() {
            return maxLat;
        }

        double x(double lon) {
            return originX + (lon - minLon) * lonScale * scale;
        }

        double y(double lat) {
            return originY + (maxLat - lat) * scale;
        }

        double lon(double x) {
            return minLon + (x - originX) / (lonScale * scale);
        }

        double lat(double y) {
            return maxLat - (y - originY) / scale;
        }
    }

    private record LabeledRing(String label, List<double[]> ring) {
    }
}
