package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TimetableGeneratorItemQueryService {

    private final TimetableGeneratorItemRepository timetableGeneratorItemRepository;

    public List<TimetableGeneratorItemEntity> findAllByTimetableGenerator(TimetableGeneratorEntity timetableGeneratorEntity) {
        return timetableGeneratorItemRepository.findByTimetableGenerator(timetableGeneratorEntity);
    }
}
