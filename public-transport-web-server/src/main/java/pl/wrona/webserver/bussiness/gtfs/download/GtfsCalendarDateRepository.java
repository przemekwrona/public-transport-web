package pl.wrona.webserver.bussiness.gtfs.download;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.core.calendar.CalendarDatesEntity;
import pl.wrona.webserver.core.calendar.CalendarDatesId;
import pl.wrona.webserver.core.calendar.CalendarEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface GtfsCalendarDateRepository extends JpaRepository<CalendarDatesEntity, CalendarDatesId> {

    List<CalendarDatesEntity> findAllByCalendarIn(Collection<CalendarEntity> calendars);
}
