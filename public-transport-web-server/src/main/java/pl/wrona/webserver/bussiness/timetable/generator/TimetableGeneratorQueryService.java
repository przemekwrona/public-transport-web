package pl.wrona.webserver.bussiness.timetable.generator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.wrona.webserver.core.timetable.TimetableGeneratorEntity;
import pl.wrona.webserver.core.timetable.TimetableGeneratorRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class TimetableGeneratorQueryService {

    private final TimetableGeneratorRepository timetableGeneratorRepository;

    public List<TimetableGeneratorEntity> findAll(String instance) {
        return timetableGeneratorRepository.findAllByAgencyName(instance);
    }
}
