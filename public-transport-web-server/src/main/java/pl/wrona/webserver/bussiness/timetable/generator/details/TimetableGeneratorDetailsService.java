package pl.wrona.webserver.bussiness.timetable.generator.details;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.GetTimetableGeneratorDetailsResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorId;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorItemQueryService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorQueryService;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TimetableGeneratorDetailsService {

    private final TimetableGeneratorQueryService timetableGeneratorQueryService;
    private final TimetableGeneratorItemQueryService timetableGeneratorItemQueryService;

    @Transactional
    @PreAgencyAuthorize
    public GetTimetableGeneratorDetailsResponse findTimetableGeneratorDetails(String instance, TimetableGeneratorId timetableGeneratorId) {
        var timetable = timetableGeneratorQueryService.findByAgencyAndRouteIdAndCreateDate(instance, timetableGeneratorId.getRouteId(), timetableGeneratorId.getCreatedAt());
        var items = timetableGeneratorItemQueryService.findAllByTimetableGenerator(timetable);

        return new GetTimetableGeneratorDetailsResponse()
                .timetableGeneratorId(new TimetableGeneratorId()
                        .routeId(timetableGeneratorId.getRouteId())
                        .timetableVersion(timetableGeneratorId.getTimetableVersion())
                        .createdAt(timetableGeneratorId.getCreatedAt()))
                .routeId(timetableGeneratorId.getRouteId())
                .timetables(new TimetableGeneratorPayload()
                        .calendarName(items.stream().findFirst().map(TimetableGeneratorItemEntity::getCalendar).map(CalendarEntity::getCalendarName).orElse("")));
    }
}
