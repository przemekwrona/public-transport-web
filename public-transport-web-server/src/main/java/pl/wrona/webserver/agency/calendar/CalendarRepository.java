package pl.wrona.webserver.agency.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.agency.entity.Agency;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {

    List<CalendarEntity> findAllByAgency(Agency agency);
}
