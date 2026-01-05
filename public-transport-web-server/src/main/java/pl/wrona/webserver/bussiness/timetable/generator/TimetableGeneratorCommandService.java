package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorItemRepository;

@Service
@AllArgsConstructor
public class TimetableGeneratorCommandService {

    private final TimetableGeneratorItemRepository timetableGeneratorItemRepository;

    public TimetableGeneratorItemEntity save(TimetableGeneratorItemEntity entity) {
        return timetableGeneratorItemRepository.save(entity);
    }
}
