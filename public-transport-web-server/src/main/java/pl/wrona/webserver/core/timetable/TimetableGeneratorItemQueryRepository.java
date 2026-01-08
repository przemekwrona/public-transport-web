package pl.wrona.webserver.core.timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableGeneratorItemQueryRepository extends JpaRepository<TimetableGeneratorItemEntity, Long> {

    List<TimetableGeneratorItemEntity> findByTimetableGenerator(TimetableGeneratorEntity timetableGenerator);
}
