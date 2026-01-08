package pl.wrona.webserver.core.timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimetableGeneratorItemCommandRepository extends JpaRepository<TimetableGeneratorItemEntity, Long> {

    void deleteAllByTimetableGenerator(TimetableGeneratorEntity timetableGenerator);
}
