package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemRepository;
import pl.wrona.webserver.core.timetable.TimetableGeneratorRepository;

@Service
@AllArgsConstructor
public class TimetableGeneratorCommandService {

    private final TimetableGeneratorRepository timetableGeneratorRepository;

    public TimetableGeneratorEntity save(TimetableGeneratorEntity entity) {
        return timetableGeneratorRepository.save(entity);
    }
}
