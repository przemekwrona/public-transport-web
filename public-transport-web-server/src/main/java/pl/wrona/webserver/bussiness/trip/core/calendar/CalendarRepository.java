package pl.wrona.webserver.bussiness.trip.core.calendar;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wrona.webserver.bussiness.trip.core.agency.AgencyEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CalendarRepository extends JpaRepository<CalendarEntity, Long> {

    List<CalendarEntity> findAllByAgency(AgencyEntity agencyEntity);

    Optional<CalendarEntity> findByAgencyAndCalendarName(AgencyEntity agencyEntity, String calendarName);

    boolean existsByAgencyAndCalendarName(AgencyEntity agencyEntity, String calendarName);

    List<CalendarEntity> findAllByAgencyAndCalendarNameStartingWith(AgencyEntity agencyEntity, String calendarName);

    List<CalendarEntity> findAllByAgencyAndStartDateBeforeAndEndDateAfter(AgencyEntity agencyEntity, LocalDate startDate, LocalDate endDate);
}
