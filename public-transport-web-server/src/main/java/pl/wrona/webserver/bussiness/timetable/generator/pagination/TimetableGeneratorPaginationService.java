package pl.wrona.webserver.bussiness.timetable.generator.pagination;

import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorCalendar;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFindAllItem;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorFindAllResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorItemQueryService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorQueryService;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.security.PreAgencyAuthorize;

@Service
@AllArgsConstructor
public class TimetableGeneratorPaginationService {

    private final TimetableGeneratorQueryService timetableGeneratorQueryService;
    private final TimetableGeneratorItemQueryService timetableGeneratorItemQueryService;

    @PreAgencyAuthorize
    @Transactional(readOnly = true)
    public TimetableGeneratorFindAllResponse findAllPaginated(String instance, int page, int size) {
        var items = this.timetableGeneratorQueryService.findAll(instance).stream()
                .map(element -> new TimetableGeneratorFindAllItem()
                        .routeLine(element.getRouteLine())
                        .routeName(element.getRouteName())
                        .routeVersion(element.getRouteVersion())
                        .calendarNames(timetableGeneratorItemQueryService.findAllByTimetableGenerator(element).stream()
                                .map(TimetableGeneratorItemEntity::getCalendar)
                                .map(calendar -> new TimetableGeneratorCalendar()
                                        .description(calendar.getDescription())
                                        .designation(calendar.getDesignation()))
                                .toList())
                        .createdAt(element.getCreatedAt()))
                .toList();

        return new TimetableGeneratorFindAllResponse()
                .items(items);
    }

}
