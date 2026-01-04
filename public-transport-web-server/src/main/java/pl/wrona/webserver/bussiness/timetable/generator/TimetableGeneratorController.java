package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TimetableGeneratorApi;
import org.igeolab.iot.pt.server.api.model.CreateTimetableGeneratorRequest;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutesResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.igeolab.iot.pt.server.api.model.TimetablePayload;
import org.igeolab.iot.pt.server.api.model.TripFilter;
import org.igeolab.iot.pt.server.api.model.TripResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.timetable.generator.creator.CreateTimetableGeneratorService;
import pl.wrona.webserver.bussiness.timetable.generator.routes.TimetableRoutePaginationService;
import pl.wrona.webserver.bussiness.timetable.generator.trips.TimetableTripPaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TimetableGeneratorController implements TimetableGeneratorApi {


    private final TimetableRoutePaginationService timetableRoutePaginationService;
    private final CreateTimetableGeneratorService createTimetableGeneratorService;
    private final TimetableTripPaginationService timetableTripPaginationService;

    @Override
    public ResponseEntity<Void> agencyAgencyTimetableGeneratorDelete(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorPayload> agencyAgencyTimetableGeneratorGet(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<Void> agencyAgencyTimetableGeneratorPut(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<CreateTimetableGeneratorRequest> createTimetableGenerator(String agency, CreateTimetableGeneratorRequest createTimetableGeneratorRequest) {
        return ResponseEntity.ok(createTimetableGeneratorService.createTimetableGenerator(agency, createTimetableGeneratorRequest));
    }

    @Override
    public ResponseEntity<TimetablePayload> findCalendars(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorFilterByRoutesResponse> findRoutes(String agency) {
        return ResponseEntity.ok(timetableRoutePaginationService.findRouteByFilter(agency));
    }

    @Override
    public ResponseEntity<TripResponse> findTrips(String agency, TripFilter tripFilter) {
        return ResponseEntity.ok(timetableTripPaginationService.findTrips(agency, tripFilter));
    }

}
