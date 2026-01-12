package pl.wrona.webserver.bussiness.timetable.generator.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetTimetableGeneratorDetailsResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorId;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TimetableGeneratorDetailsService {

    private final TimetableGeneratorQueryService timetableGeneratorQueryService;

    @PreAgencyAuthorize
    public GetTimetableGeneratorDetailsResponse findTimetableGeneratorDetails(String instance, TimetableGeneratorId timetableGeneratorId) {
        var timetable = timetableGeneratorQueryService.findByAgencyAndRouteIdAndCreateDate(instance, timetableGeneratorId.getRouteId(), timetableGeneratorId.getCreatedAt());

        return new GetTimetableGeneratorDetailsResponse()
                .timetableGeneratorId(new TimetableGeneratorId()
                        .routeId(timetableGeneratorId.getRouteId())
                        .timetableVersion(timetableGeneratorId.getTimetableVersion())
                        .createdAt(timetableGeneratorId.getCreatedAt()))
                .timetables(new TimetableGeneratorPayload()
                        .calendarName(""));
    }
}
