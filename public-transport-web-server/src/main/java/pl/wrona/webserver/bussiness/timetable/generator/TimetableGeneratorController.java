package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TimetableGeneratorApi;
import org.igeolab.iot.pt.server.api.model.CreateTimetableGeneratorRequest;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorDeletionRequest;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutesResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFindAllResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.igeolab.iot.pt.server.api.model.TimetablePayload;
import org.igeolab.iot.pt.server.api.model.TripFilter;
import org.igeolab.iot.pt.server.api.model.TripResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.timetable.generator.creator.CreateTimetableGeneratorService;
import pl.wrona.webserver.bussiness.timetable.generator.deletion.TimetableGeneratorDeletionService;
import pl.wrona.webserver.bussiness.timetable.generator.pagination.TimetableGeneratorPaginationService;
import pl.wrona.webserver.bussiness.timetable.generator.routes.TimetableRoutePaginationService;
import pl.wrona.webserver.bussiness.timetable.generator.trips.TimetableTripPaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TimetableGeneratorController implements TimetableGeneratorApi {


    private final TimetableRoutePaginationService timetableRoutePaginationService;
    private final CreateTimetableGeneratorService createTimetableGeneratorService;
    private final TimetableTripPaginationService timetableTripPaginationService;
    private final TimetableGeneratorPaginationService timetableGeneratorPaginationService;
    private final TimetableGeneratorDeletionService timetableGeneratorDeletionService;

    @Override
    public ResponseEntity<Status> deleteTimetableGenerator(String agency, TimetableGeneratorDeletionRequest timetableGeneratorDeletionRequest) {
        return ResponseEntity.ok(timetableGeneratorDeletionService.agencyAgencyTimetableGeneratorDelete(agency, timetableGeneratorDeletionRequest));
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
    public ResponseEntity<TimetableGeneratorFindAllResponse> findAll(String agency) {
        return ResponseEntity.ok(timetableGeneratorPaginationService.findAllPaginated(agency, 0, 100));
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
