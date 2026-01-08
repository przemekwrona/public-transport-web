package pl.wrona.webserver.bussiness.timetable.generator.deletion;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.Status;
import org.igeolab.iot.pt.server.api.model.TimetableGeneratorDeletionRequest;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorCommandService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorItemCommandService;
import pl.wrona.webserver.bussiness.timetable.generator.TimetableGeneratorQueryService;
import pl.wrona.webserver.security.PreAgencyAuthorize;

import java.util.Optional;

@Service
@AllArgsConstructor
public class TimetableGeneratorDeletionService {

    private final TimetableGeneratorQueryService timetableGeneratorQueryService;
    private final TimetableGeneratorItemCommandService timetableGeneratorItemCommandService;
    private final TimetableGeneratorCommandService timetableGeneratorCommandService;

    @Transactional
    @PreAgencyAuthorize
    public Status agencyAgencyTimetableGeneratorDelete(String instance, TimetableGeneratorDeletionRequest timetableGeneratorDeletionRequest) {
        Optional.ofNullable(timetableGeneratorQueryService.findByAgencyAndRouteIdAndCreateDate(instance, timetableGeneratorDeletionRequest.getRouteId(), timetableGeneratorDeletionRequest.getCreatedDate())).ifPresent(timetableGenerator -> {
            timetableGeneratorItemCommandService.deleteAllByTimetableGenerator(timetableGenerator);
            timetableGeneratorCommandService.delete(timetableGenerator);
        });

        return new Status()
                .status(Status.StatusEnum.DELETED);
    }
}
