package pl.wrona.webserver.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CalendarItemQueryRepository extends JpaRepository<CalendarItemEntity, Long> {
}
