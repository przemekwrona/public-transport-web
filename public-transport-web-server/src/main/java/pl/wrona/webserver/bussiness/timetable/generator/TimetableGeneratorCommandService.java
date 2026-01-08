package pl.wrona.webserver.bussiness.timetable.generator;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorCommandRepository;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorQueryRepository;

@Service
@AllArgsConstructor
public class TimetableGeneratorCommandService {

    private final TimetableGeneratorCommandRepository timetableGeneratorCommandRepository;

    public TimetableGeneratorEntity save(TimetableGeneratorEntity entity) {
        return timetableGeneratorCommandRepository.save(entity);
    }

    @Transactional
    public void delete(TimetableGeneratorEntity entity) {
        timetableGeneratorCommandRepository.delete(entity);
    }
}
