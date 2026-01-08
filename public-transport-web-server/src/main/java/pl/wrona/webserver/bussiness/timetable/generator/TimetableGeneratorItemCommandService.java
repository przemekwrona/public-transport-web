package pl.wrona.webserver.bussiness.timetable.generator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemCommandRepository;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemQueryRepository;

@Service
@AllArgsConstructor
public class TimetableGeneratorItemCommandService {

    private final TimetableGeneratorItemQueryRepository timetableGeneratorItemRepository;
    private final TimetableGeneratorItemCommandRepository timetableGeneratorItemCommandRepository;

    public TimetableGeneratorItemEntity save(TimetableGeneratorItemEntity entity) {
        return timetableGeneratorItemRepository.save(entity);
    }

    @Transactional
    public void deleteAllByTimetableGenerator(TimetableGeneratorEntity timetableGenerator) {
        timetableGeneratorItemCommandRepository.deleteAllByTimetableGenerator(timetableGenerator);
    }
}
