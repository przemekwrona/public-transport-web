package pl.wrona.webserver.bussiness.trip.mappdf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Point2D;
import org.igeolab.iot.pt.server.api.model.Subdivision;
import org.igeolab.iot.pt.server.api.model.SubdivisionByStopsResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.subdivision.SubdivisionQueryService;
import pl.wrona.webserver.bussiness.trip.TripQueryService;
import pl.wrona.webserver.core.StopService;
import pl.wrona.webserver.core.agency.TripEntity;
import pl.wrona.webserver.core.agency.TripGeometryEntity;
import pl.wrona.webserver.core.stop.StopEntity;
import pl.wrona.webserver.exception.BusinessException;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TripMapPdfService {

    private final TripQueryService tripQueryService;
    private final StopService stopService;
    private final SubdivisionQueryService subdivisionQueryService;
    private final TripMapPdfRenderer tripMapPdfRenderer;
    private final ObjectMapper objectMapper;

    @PreAgencyAuthorize(roles = {
            PreAgencyAuthorize.Role.SUPER_USER,
            PreAgencyAuthorize.Role.AGENCY_OWNER,
            PreAgencyAuthorize.Role.PLANNER,
            PreAgencyAuthorize.Role.BRIGADIER,
            PreAgencyAuthorize.Role.DISPATCHER,
            PreAgencyAuthorize.Role.DRIVER
    })
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadTripMapPdf(String instance, String routeCode, String tripCode) {
        TripEntity trip = tripQueryService.findTripByAgencyAndRouteCodeAndTripCode(instance, routeCode, tripCode);
        List<Point2D> geometry = buildGeometry(geometryOf(trip));
        List<Long> stopIds = stopService.findStopByTripId(trip.getTripId()).stream()
                .map(StopEntity::getStopId)
                .toList();

        SubdivisionByStopsResponse subdivisionsResponse = subdivisionQueryService.findSubdivisionsByStopIds(stopIds);
        List<Subdivision> subdivisions = subdivisionsResponse.getSubdivisions() == null
                ? List.of()
                : subdivisionsResponse.getSubdivisions();

        List<TripMapPdfView.Municipality> municipalities =
                TripMunicipalityRouteStats.withDistanceShares(geometry, subdivisions);

        TripMapPdfView view = new TripMapPdfView(
                instance,
                trip.getRoute().getLine(),
                trip.getRoute().getName(),
                trip.getTripCode(),
                trip.getVariantDesignation(),
                trip.getHeadsign(),
                TripMapImageFactory.buildHtmlImage(geometry, subdivisions),
                municipalities
        );

        byte[] pdf = tripMapPdfRenderer.render(view);
        String filename = "mapa-trasy-%s-%s.pdf".formatted(routeCode, tripCode);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"%s\"".formatted(filename))
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }

    private String geometryOf(TripEntity tripEntity) {
        TripGeometryEntity tripGeometry = tripEntity.getTripGeometry();
        if (tripGeometry == null) {
            return null;
        }
        return tripGeometry.getGeometry();
    }

    private List<Point2D> buildGeometry(String path) {
        List<Point2D> geometry = new ArrayList<>();
        if (path == null || path.isBlank()) {
            return geometry;
        }
        try {
            List<List<Float>> points = objectMapper.readValue(path, new TypeReference<>() {
            });
            return points.stream()
                    .map(point -> new Point2D().lat(point.get(0)).lon(point.get(1)))
                    .toList();
        } catch (Exception exception) {
            throw new BusinessException("ERROR:202611140012", exception.getMessage());
        }
    }
}
