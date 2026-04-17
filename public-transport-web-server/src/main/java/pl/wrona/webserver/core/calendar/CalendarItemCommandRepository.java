package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarItemCommandRepository extends JpaRepository<CalendarItemEntity, Long> {
}
