package pl.wrona.webserver.bussiness.timetable.generator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.igeolab.iot.pt.server.api.model.RouteId;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorQueryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class TimetableGeneratorQueryService {

    private final TimetableGeneratorQueryRepository timetableGeneratorQueryRepository;

    public List<TimetableGeneratorEntity> findAll(String instance) {
        return timetableGeneratorQueryRepository.findAllByAgencyName(instance);
    }

    @Transactional
    public TimetableGeneratorEntity findByAgencyAndRouteIdAndCreateDate(String instance, RouteId routeId, LocalDateTime createdAt) {
        return this.timetableGeneratorQueryRepository.findByAgencyAndRouteIdAndCreateDate(instance, routeId.getLine(), routeId.getName(), routeId.getVersion(), createdAt);
    }
}
