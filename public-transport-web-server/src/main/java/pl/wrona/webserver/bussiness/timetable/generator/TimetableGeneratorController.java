package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.TimetableGeneratorApi;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFilterByRoutesResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.bussiness.timetable.generator.routes.TimetableRoutePaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class TimetableGeneratorController implements TimetableGeneratorApi {


    private final TimetableRoutePaginationService timetableRoutePaginationService;

    @Override
    public ResponseEntity<Void> agencyAgencyTimetableGeneratorDelete(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorPayload> agencyAgencyTimetableGeneratorGet(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorPayload> agencyAgencyTimetableGeneratorPost(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<Void> agencyAgencyTimetableGeneratorPut(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorPayload> findCalendars(String agency) {
        return null;
    }

    @Override
    public ResponseEntity<TimetableGeneratorFilterByRoutesResponse> findRoutes(String agency) {
        return ResponseEntity.ok(timetableRoutePaginationService.findRouteByFilter(agency));
    }

}
