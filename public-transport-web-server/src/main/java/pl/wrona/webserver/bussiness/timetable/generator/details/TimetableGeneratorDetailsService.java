package pl.wrona.webserver.bussiness.timetable.generator.details;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.igeolab.iot.pt.server.api.model.GetTimetableGeneratorDetailsResponse;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorId;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorPayload;
import org.igeolab.iot.pt.server.api.model.TimetablePayload;
import org.igeolab.iot.pt.server.api.model.TimetableStopTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorItemQueryService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorQueryService;
import pl.wrona.webserver.core.calendar.CalendarEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class TimetableGeneratorDetailsService {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");


    private final TimetableGeneratorQueryService timetableGeneratorQueryService;
    private final TimetableGeneratorItemQueryService timetableGeneratorItemQueryService;

    @Transactional
    @PreAgencyAuthorize
    public GetTimetableGeneratorDetailsResponse findTimetableGeneratorDetails(String instance, TimetableGeneratorId timetableGeneratorId) {
        var timetable = timetableGeneratorQueryService.findByAgencyAndRouteIdAndCreateDate(instance, timetableGeneratorId.getRouteId(), timetableGeneratorId.getCreatedAt());
        var items = timetableGeneratorItemQueryService.findAllByTimetableGenerator(timetable);
        var firstItem = items.stream().findFirst();

        return new GetTimetableGeneratorDetailsResponse()
                .timetableGeneratorId(new TimetableGeneratorId()
                        .routeId(timetableGeneratorId.getRouteId())
                        .timetableVersion(timetableGeneratorId.getTimetableVersion())
                        .createdAt(timetableGeneratorId.getCreatedAt()))
                .routeId(timetableGeneratorId.getRouteId())
                .timetables(new TimetableGeneratorPayload()
                        .calendarName(items.stream().findFirst().map(TimetableGeneratorItemEntity::getCalendar).map(CalendarEntity::getCalendarName).orElse(""))
                        .front(new TimetablePayload()
                                .startDate(firstItem
                                        .map(TimetableGeneratorItemEntity::getFrontStartTime)
                                        .map(time -> time.format(FORMATTER))
                                        .orElse(StringUtils.EMPTY))
                                .endDate(firstItem
                                        .map(TimetableGeneratorItemEntity::getFrontEndTime)
                                        .map(time -> time.format(FORMATTER))
                                        .orElse(StringUtils.EMPTY))
                                .interval(firstItem
                                        .map(TimetableGeneratorItemEntity::getFrontInterval)
                                        .orElse(null))
                                .departures(firstItem
                                        .map(TimetableGeneratorItemEntity::getFrontPayload)
                                        .orElse(List.of()).stream()
                                        .map(timetableDeparture -> new TimetableStopTime().time(timetableDeparture.getTime().format(FORMATTER)).designation(timetableDeparture.getSymbol()))
                                        .toList()))
                        .back(new TimetablePayload()
                                .startDate(firstItem
                                        .map(TimetableGeneratorItemEntity::getBackStartTime)
                                        .map(time -> time.format(FORMATTER))
                                        .orElse(StringUtils.EMPTY))
                                .endDate(firstItem
                                        .map(TimetableGeneratorItemEntity::getBackEndTime)
                                        .map(time -> time.format(FORMATTER))
                                        .orElse(StringUtils.EMPTY))
                                .interval(firstItem
                                        .map(TimetableGeneratorItemEntity::getBackInterval)
                                        .orElse(null))
                                .departures(firstItem
                                        .map(TimetableGeneratorItemEntity::getBackPayload)
                                        .orElse(List.of()).stream()
                                        .map(timetableDeparture -> new TimetableStopTime().time(timetableDeparture.getTime().format(FORMATTER)).designation(timetableDeparture.getSymbol())).toList())));
    }
}
