package pl.wrona.webserver.bussiness.brigade;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.BrigadeApi;
import org.igeolab.iot.pt.server.api.model.BrigadeBody;
import org.igeolab.iot.pt.server.api.model.BrigadeBodyV2;
import org.igeolab.iot.pt.server.api.model.BrigadeDeleteBody;
import org.igeolab.iot.pt.server.api.model.BrigadePatchBody;
import org.igeolab.iot.pt.server.api.model.BrigadePayload;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeRequest;
import org.igeolab.iot.pt.server.api.model.CreateCalendarSymbolBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.GetBrigadeResponse;
import org.igeolab.iot.pt.server.api.model.NextBrigadeEventSequenceResponse;
import org.igeolab.iot.pt.server.api.model.NextCalendarResourceSequenceResponse;
import org.igeolab.iot.pt.server.api.model.PutBrigadeEventBody;
import org.igeolab.iot.pt.server.api.model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.wrona.webserver.Hex;
import pl.wrona.webserver.bussiness.brigade.details.BrigadeGroupDetailsService;
import pl.wrona.webserver.bussiness.brigade.event.creator.BrigadeEventCreatorService;
import pl.wrona.webserver.bussiness.brigade.event.updater.BrigadeEventUpdaterService;
import pl.wrona.webserver.bussiness.brigade.group.creator.BrigadeGroupCreatorService;
import pl.wrona.webserver.bussiness.brigade.group.pagination.BrigadePaginationService;

@RestController
@AllArgsConstructor
@RequestMapping("${webserver.context.path}")
public class BrigadeController implements BrigadeApi {

    private final BrigadeQueryService brigadeQueryService;
    private final BrigadeGroupCreatorService brigadeGroupCreatorService;
    private final BrigadePaginationService brigadePaginationService;
    private final BrigadeGroupDetailsService brigadeGroupDetailsService;
    private final BrigadeEventCreatorService brigadeEventCreatorService;
    private final BrigadeEventUpdaterService brigadeEventUpdaterService;

    @Override
    public ResponseEntity<GetBrigadeResponse> getBrigades(String agency) {
        return ResponseEntity.ok(brigadePaginationService.findBrigades(agency));
    }

    @Override
    public ResponseEntity<BrigadeBodyV2> getCalendarSymbolBrigadeResources(String agency, String calendarCode, String symbol) {
        return ResponseEntity.ok(brigadeGroupDetailsService.getCalendarSymbolBrigadeResources(agency, calendarCode, symbol));
    }

    @Override
    public ResponseEntity<NextBrigadeEventSequenceResponse> getNextBrigadeEventSequence(String agency, String calendarCode, String symbol, String resourceCode) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brigadeEventCreatorService.getNextBrigadeEventSequence(agency, 1, calendarCode, symbol, resourceCode));
    }

    @Override
    public ResponseEntity<NextCalendarResourceSequenceResponse> getNextCalendarResourceSequence(String agency, String calendarCode, String symbol) {
        return ResponseEntity.badRequest().build();
    }

    @Override
    public ResponseEntity<Status> putBrigadeEvent(String agency, String calendarCode, String symbol, String resourceCode, PutBrigadeEventBody putBrigadeEventBody) {
        return ResponseEntity.ok(brigadeEventUpdaterService.putBrigadeEvent(agency, calendarCode, symbol, resourceCode, putBrigadeEventBody));
    }

    @Override
    public ResponseEntity<Status> createBrigade(String agency, BrigadeBody createBrigadeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brigadeQueryService.createBrigade(agency, createBrigadeRequest));
    }

    @Override
    public ResponseEntity<CreateCalendarSymbolBrigadeResponse> createCalendarSymbolBrigade(String agency, String brigadeCode, String calendarCode, String calendarSymbol, CreateCalendarSymbolBrigadeRequest createCalendarSymbolBrigadeRequest) {
        return ResponseEntity.ok(brigadeGroupCreatorService.createBrigadeGroup(agency, brigadeCode, calendarCode, calendarSymbol, createCalendarSymbolBrigadeRequest));
    }

    @Override
    public ResponseEntity<Status> deleteBrigade(String agency, BrigadeDeleteBody brigadeDeleteBody) {
        return ResponseEntity.status(HttpStatus.OK).body(brigadeQueryService.deleteBrigade(agency, brigadeDeleteBody));
    }

    @Override
    public ResponseEntity<BrigadeBody> getBrigadeByBrigadeName(String agency, BrigadePayload brigadePayload) {
        return ResponseEntity.ok(brigadeGroupDetailsService.getBrigadeByBrigadeName(agency, brigadePayload));
    }

    @Override
    public ResponseEntity<Status> updateBrigade(String agency, BrigadePatchBody brigadePatchBody) {
        return ResponseEntity.ok(brigadeQueryService.updateBrigade(agency, brigadePatchBody));
    }
}
